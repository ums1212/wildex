package dev.comon.wildex.journal.birdlist

import dev.comon.wildex.domain.model.BirdSummary

data class BirdListUiState(
    val items: List<BirdSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val pageNo: Int = 1,
)

sealed interface BirdListIntent {
    data object LoadInitial : BirdListIntent
    data object LoadMore : BirdListIntent
    data object Retry : BirdListIntent
    data class BirdClicked(val speciesId: String) : BirdListIntent
}

sealed interface BirdListUiEvent {
    data class NavigateToBirdInfo(val speciesId: String) : BirdListUiEvent
    data class ShowError(val message: String) : BirdListUiEvent
}
