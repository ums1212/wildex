package dev.comon.wildex.journal.birdlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.BirdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

class BirdListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BirdRepository(application)

    private val _state = MutableStateFlow(BirdListUiState())
    val state: StateFlow<BirdListUiState> = _state.asStateFlow()

    private val _events = Channel<BirdListUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        onIntent(BirdListIntent.LoadInitial)
    }

    fun onIntent(intent: BirdListIntent) {
        when (intent) {
            BirdListIntent.LoadInitial -> loadPage(page = 1, reset = true)
            BirdListIntent.Retry -> loadPage(page = 1, reset = true)
            BirdListIntent.LoadMore -> {
                val s = _state.value
                if (!s.isLoadingMore && !s.isLoading && s.hasMore) {
                    loadPage(page = s.pageNo + 1, reset = false)
                }
            }
            is BirdListIntent.BirdClicked -> viewModelScope.launch {
                _events.send(BirdListUiEvent.NavigateToBirdInfo(intent.speciesId))
            }
            BirdListIntent.EnterSearchMode -> _state.update { it.copy(isSearchMode = true) }
            BirdListIntent.ExitSearchMode -> _state.update {
                it.copy(
                    isSearchMode = false,
                    pendingQuery = "",
                    submittedQuery = null,
                    searchResults = emptyList(),
                    searchError = null,
                    isSearching = false,
                )
            }
            is BirdListIntent.PendingQueryChanged -> _state.update {
                it.copy(pendingQuery = intent.query)
            }
            BirdListIntent.SubmitSearch -> {
                val name = _state.value.pendingQuery.trim().ifEmpty { null } ?: return
                _state.update { it.copy(submittedQuery = name) }
                runSearch(name)
            }
        }
    }

    fun enterSearchMode() = onIntent(BirdListIntent.EnterSearchMode)
    fun exitSearchMode() = onIntent(BirdListIntent.ExitSearchMode)
    fun onPendingQueryChange(query: String) = onIntent(BirdListIntent.PendingQueryChanged(query))
    fun submitSearch() = onIntent(BirdListIntent.SubmitSearch)

    private fun runSearch(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }
            runCatching {
                repository.searchListByName(name)
            }.onSuccess { results ->
                _state.update { it.copy(isSearching = false, searchResults = results) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isSearching = false,
                        searchResults = emptyList(),
                        searchError = e.message ?: "검색 실패",
                    )
                }
            }
        }
    }

    private fun loadPage(page: Int, reset: Boolean) {
        viewModelScope.launch {
            _state.update { s ->
                if (reset) s.copy(isLoading = true, error = null, items = emptyList())
                else s.copy(isLoadingMore = true, error = null)
            }
            runCatching {
                repository.getBirdListPage(page, PAGE_SIZE)
            }.onSuccess { result ->
                _state.update { s ->
                    val merged = if (reset) result.items else s.items + result.items
                    s.copy(
                        items = merged,
                        isLoading = false,
                        isLoadingMore = false,
                        pageNo = page,
                        hasMore = result.hasNext,
                        error = null,
                    )
                }
            }.onFailure { e ->
                val msg = e.message ?: "목록을 불러올 수 없습니다"
                _state.update { s ->
                    s.copy(isLoading = false, isLoadingMore = false, error = if (reset) msg else null)
                }
                if (!reset) {
                    _events.send(BirdListUiEvent.ShowError(msg))
                }
            }
        }
    }
}
