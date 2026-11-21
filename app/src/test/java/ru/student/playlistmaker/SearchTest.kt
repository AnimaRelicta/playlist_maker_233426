package ru.student.playlistmaker

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import ru.student.playlistmaker.data.*
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.domain.TracksRepository
import ru.student.playlistmaker.ui.search.SearchResult
import ru.student.playlistmaker.ui.search.SearchViewModel
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTest {
    @Test fun localRepositoryTrimsAndMatchesTitleOrArtist() = runTest {
        val storage = TrackStorage()
        val repository = LocalTracksRepository(LocalTracksClient(storage))
        assertEquals(emptyList<Track>(), repository.search("  "))
        assertEquals(listOf(1L), repository.search("  yeSTER  ").map { it.id })
        assertEquals(8, repository.search("Beatles").size)
        assertTrue(repository.search("missing song").isEmpty())
        assertEquals("02:05", repository.findById(1)?.duration)
        assertNull(repository.findById(-1))
        assertEquals("00:00", Track(9, "Title", "Artist", 0, Track.Artwork.NONE).duration)
        assertEquals("61:02", Track(9, "Title", "Artist", 3662000, Track.Artwork.NONE).duration)
    }

    @Test fun clearAndNewQueryIgnoreCancelledResults() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            val storage = TrackStorage()
            val vm = SearchViewModel(LocalTracksRepository(LocalTracksClient(storage)), dispatcher)
            store.put("search", vm)
            assertEquals(SearchResult.Initial, vm.state.value.result)
            vm.changeQuery("Beatles")
            vm.search()
            assertEquals(SearchResult.Searching, vm.state.value.result)
            runCurrent()
            vm.changeQuery("")
            advanceUntilIdle()
            assertEquals(SearchResult.Initial, vm.state.value.result)
            vm.changeQuery("Beatles")
            vm.search()
            runCurrent()
            vm.changeQuery("Michelle")
            vm.search()
            advanceUntilIdle()
            assertEquals(listOf("Michelle"), (vm.state.value.result as SearchResult.Success).tracks.map { it.title })
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test fun failureCanBeRetriedAndEmptySuccessIsDistinct() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            var calls = 0
            val vm = SearchViewModel(object : TracksRepository {
                override suspend fun search(query: String): List<Track> {
                    delay(100)
                    if (++calls == 1) throw IOException("Unavailable")
                    return emptyList()
                }
                override suspend fun findById(id: Long): Track? = null
                    override val favoriteIds = kotlinx.coroutines.flow.flowOf(emptySet<Long>())
                    override val favorites = kotlinx.coroutines.flow.flowOf(emptyList<Track>())
                    override suspend fun toggleFavorite(trackId: Long) = Unit
            }, dispatcher)
            store.put("search", vm)
            vm.search()
            assertEquals(SearchResult.Initial, vm.state.value.result)
            vm.changeQuery("unknown")
            vm.search()
            advanceUntilIdle()
            assertEquals(SearchResult.Fail("Unavailable"), vm.state.value.result)
            vm.search()
            advanceUntilIdle()
            assertEquals(SearchResult.Success(emptyList()), vm.state.value.result)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
    @Test fun lateFailureFromCancelledQueryCannotReplaceNewResults() = runTest {
        val main = StandardTestDispatcher(testScheduler, "UI")
        val io = StandardTestDispatcher(testScheduler, "repository")
        Dispatchers.setMain(main)
        val store = ViewModelStore()
        try {
            val selected = Track(7, "Eleanor Rigby", "The Beatles", 198000, Track.Artwork.NONE)
            val repository = object : TracksRepository {
                override suspend fun search(query: String): List<Track> {
                    assertSame(io, kotlinx.coroutines.currentCoroutineContext()[kotlin.coroutines.ContinuationInterceptor])
                    if (query == "old") {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) { delay(3000) }
                        throw IOException("Late response")
                    }
                    return listOf(selected)
                }
                override suspend fun findById(id: Long): Track? = null
                    override val favoriteIds = kotlinx.coroutines.flow.flowOf(emptySet<Long>())
                    override val favorites = kotlinx.coroutines.flow.flowOf(emptyList<Track>())
                    override suspend fun toggleFavorite(trackId: Long) = Unit
            }
            val vm = SearchViewModel(repository, io)
            store.put("search", vm)
            vm.changeQuery("old")
            vm.search()
            runCurrent()
            vm.changeQuery("Eleanor")
            vm.search()
            runCurrent()
            val expected = SearchResult.Success(listOf(selected))
            assertEquals(expected, vm.state.value.result)
            advanceUntilIdle()
            assertEquals("Eleanor", vm.state.value.query)
            assertEquals(expected, vm.state.value.result)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

}
