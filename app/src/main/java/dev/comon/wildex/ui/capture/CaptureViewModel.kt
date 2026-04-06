package dev.comon.wildex.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val ZoomStep = 0.2f

class CaptureViewModel : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState())
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    private val _events = Channel<CaptureUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onIntent(intent: CaptureIntent) {
        when (intent) {
            is CaptureIntent.PermissionResult ->
                _state.update { it.copy(hasCameraPermission = intent.granted) }
            CaptureIntent.FlashOn -> _state.update { it.copy(flashOn = true) }
            CaptureIntent.FlashOff -> _state.update { it.copy(flashOn = false) }
            CaptureIntent.ZoomIn -> _state.update { s ->
                s.copy(
                    zoomRatio = (s.zoomRatio + ZoomStep).coerceIn(s.zoomMin, s.zoomMax),
                )
            }
            CaptureIntent.ZoomOut -> _state.update { s ->
                s.copy(
                    zoomRatio = (s.zoomRatio - ZoomStep).coerceIn(s.zoomMin, s.zoomMax),
                )
            }
            CaptureIntent.ToggleIso -> _state.update { it.copy(isoAuto = !it.isoAuto) }
            CaptureIntent.ToggleWb -> _state.update { it.copy(wbDay = !it.wbDay) }
            CaptureIntent.CaptureClicked -> viewModelScope.launch {
                _events.send(CaptureUiEvent.TakePicture)
            }
            is CaptureIntent.ZoomBoundsUpdated -> _state.update { s ->
                val min = intent.min
                val max = intent.max
                val z = s.zoomRatio.coerceIn(min, max)
                s.copy(zoomMin = min, zoomMax = max, zoomRatio = z)
            }
            is CaptureIntent.CameraBindFailed -> viewModelScope.launch {
                _events.send(
                    CaptureUiEvent.ShowError(
                        intent.error.message ?: "카메라를 시작할 수 없습니다",
                    ),
                )
            }
            CaptureIntent.CaptureSucceeded -> { }
            is CaptureIntent.CaptureFailed -> viewModelScope.launch {
                _events.send(
                    CaptureUiEvent.ShowError(
                        intent.error.message ?: "촬영에 실패했습니다",
                    ),
                )
            }
        }
    }
}
