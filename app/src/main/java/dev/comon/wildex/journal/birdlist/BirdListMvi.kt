package dev.comon.wildex.journal.birdlist

import dev.comon.wildex.domain.model.BirdSummary

data class BirdListUiState(
    val items: List<BirdSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val pageNo: Int = 1,
    val isSearchMode: Boolean = false,
    val pendingQuery: String = "",
    val submittedQuery: String? = null,
    val searchResults: List<BirdSummary> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
)

sealed interface BirdListIntent {
    data object LoadInitial : BirdListIntent
    data object LoadMore : BirdListIntent
    data object Retry : BirdListIntent
    data class BirdClicked(val speciesId: String) : BirdListIntent
    data object EnterSearchMode : BirdListIntent
    data object ExitSearchMode : BirdListIntent
    data class PendingQueryChanged(val query: String) : BirdListIntent
    data object SubmitSearch : BirdListIntent
}

sealed interface BirdListUiEvent {
    data class NavigateToBirdInfo(val speciesId: String) : BirdListUiEvent
    data class ShowError(val message: String) : BirdListUiEvent
}
