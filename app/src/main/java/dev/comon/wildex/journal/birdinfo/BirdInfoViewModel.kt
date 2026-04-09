package dev.comon.wildex.journal.birdinfo

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

class BirdInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BirdRepository(application)

    private val _state = MutableStateFlow(BirdInfoUiState())
    val state: StateFlow<BirdInfoUiState> = _state.asStateFlow()

    private val _events = Channel<BirdInfoUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onIntent(intent: BirdInfoIntent) {
        when (intent) {
            is BirdInfoIntent.Load -> loadInfo(intent.speciesId)
            is BirdInfoIntent.Retry -> loadInfo(intent.speciesId)
        }
    }

    private fun loadInfo(speciesId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.getBirdInfo(speciesId)
            }.onSuccess { detail ->
                _state.update { it.copy(bird = detail, isLoading = false) }
            }.onFailure { e ->
                val msg = e.message ?: "정보를 불러올 수 없습니다"
                _state.update { it.copy(isLoading = false, error = msg) }
                _events.send(BirdInfoUiEvent.ShowError(msg))
            }
        }
    }
}
