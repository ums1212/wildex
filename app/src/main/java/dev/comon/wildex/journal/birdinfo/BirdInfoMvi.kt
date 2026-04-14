package dev.comon.wildex.journal.birdinfo

import dev.comon.wildex.domain.model.BirdDetail

data class BirdInfoUiState(
    val bird: BirdDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSpeaking: Boolean = false,
)

sealed interface BirdInfoIntent {
    data class Load(val speciesId: String) : BirdInfoIntent
    data class Retry(val speciesId: String) : BirdInfoIntent
    object ToggleTts : BirdInfoIntent
    object StopTts : BirdInfoIntent
}

sealed interface BirdInfoUiEvent {
    data class ShowError(val message: String) : BirdInfoUiEvent
    data class ShowTtsError(val message: String) : BirdInfoUiEvent
    object DuckBgm : BirdInfoUiEvent
    object RestoreBgm : BirdInfoUiEvent
}
