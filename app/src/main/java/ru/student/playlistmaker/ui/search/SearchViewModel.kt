package ru.student.playlistmaker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
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

class SearchViewModel(private val repository: TracksRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchState())
    val state = mutableState.asStateFlow()
    private var pendingSearch: Job? = null

    fun changeQuery(query: String) {
        if (query == state.value.query) return
        pendingSearch?.cancel()
        mutableState.value = SearchState(query)
    }

    fun search() {
        val query = state.value.query
        if (query.isBlank()) return
        pendingSearch?.cancel()
        mutableState.value = SearchState(query, SearchResult.Searching)
        pendingSearch = viewModelScope.launch {
            try {
                val tracks = repository.search(query)
                ensureActive()
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
