package ru.student.playlistmaker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import ru.student.playlistmaker.domain.SearchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.domain.TracksRepository

sealed interface SearchResult {
    data object Initial : SearchResult
    data object Searching : SearchResult
    data class Success(val tracks: List<Track>) : SearchResult
    data class Fail(val message: String?) : SearchResult
}

data class SearchState(val query: String = "", val result: SearchResult = SearchResult.Initial)

class SearchViewModel(
    private val repository: TracksRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val historyRepository: SearchHistoryRepository? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchState())
    val state = mutableState.asStateFlow()
    val history = historyRepository?.queries ?: flowOf(emptyList())
    private var pendingSearch: Job? = null

    fun changeQuery(query: String) {
        if (query == state.value.query) return
        pendingSearch?.cancel()
        mutableState.value = SearchState(query)
        if (query.isNotBlank()) scheduleSearch(query, 500)
    }

    fun searchFromHistory(query: String) {
        changeQuery(query)
        search()
    }

    fun search() {
        val query = state.value.query
        if (query.isBlank()) return
        scheduleSearch(query, 0)
    }

    private fun scheduleSearch(query: String, delayMillis: Long) {
        pendingSearch?.cancel()
        if (delayMillis == 0L) mutableState.value = SearchState(query, SearchResult.Searching)
        pendingSearch = viewModelScope.launch {
            if (delayMillis > 0L) {
                delay(delayMillis)
                mutableState.value = SearchState(query, SearchResult.Searching)
            }
            try {
                val tracks = withContext(ioDispatcher) { repository.search(query) }
                ensureActive()
                historyRepository?.record(query)
                mutableState.value = SearchState(query, SearchResult.Success(tracks))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                ensureActive()
                mutableState.value = SearchState(query, SearchResult.Fail(exception.message))
            }
        }
    }
}
