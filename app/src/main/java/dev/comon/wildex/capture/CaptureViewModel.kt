package dev.comon.wildex.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.AnalysisRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val ZoomStep = 0.2f

class CaptureViewModel(
    private val analysisRepository: AnalysisRepository = AnalysisRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState())
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    private val _events = Channel<CaptureUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * 카메라 셔터가 눌려 [CaptureUiEvent.TakePicture] 이벤트가 발행된 뒤,
     * 콜백 결과가 돌아오기 전까지 중복 촬영을 막는 플래그.
     * CaptureSucceeded / CaptureFailed 수신 시 초기화.
     */
    private var isCaptureInProgress = false

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

            CaptureIntent.CaptureClicked -> {
                // 촬영 진행 중이거나 분석 중이면 연타 무시
                if (isCaptureInProgress || _state.value.isAnalyzing) return
                isCaptureInProgress = true
                viewModelScope.launch {
                    _events.send(CaptureUiEvent.TakePicture)
                }
            }

            is CaptureIntent.ZoomBoundsUpdated -> _state.update { s ->
                val min = intent.min
                val max = intent.max
                val z = s.zoomRatio.coerceIn(min, max)
                s.copy(zoomMin = min, zoomMax = max, zoomRatio = z)
            }

            is CaptureIntent.CameraBindFailed -> viewModelScope.launch {
                _events.send(
                    CaptureUiEvent.ShowSnackbar(
                        AnalysisError.UNKNOWN.userMessage,
                    ),
                )
            }

            is CaptureIntent.CaptureSucceeded -> {
                isCaptureInProgress = false
                viewModelScope.launch {
                    analysisRepository.analyzeImage(intent.imageBytes, intent.rotationDegrees)
                        .catch { e ->
                            val error = mapToAnalysisError(e)
                            emit(AnalysisState.Error(error))
                        }
                        .collect { analysisState ->
                            when (analysisState) {
                                AnalysisState.Analyzing -> {
                                    _state.update { it.copy(isAnalyzing = true) }
                                }
                                is AnalysisState.Success -> {
                                    _state.update { it.copy(isAnalyzing = false) }
                                    _events.send(
                                        CaptureUiEvent.NavigateToBirdInfo(analysisState.speciesId),
                                    )
                                }
                                is AnalysisState.Error -> {
                                    _state.update { it.copy(isAnalyzing = false) }
                                    _events.send(
                                        CaptureUiEvent.ShowSnackbar(analysisState.error.userMessage),
                                    )
                                }
                            }
                        }
                }
            }

            is CaptureIntent.CaptureFailed -> {
                isCaptureInProgress = false
                viewModelScope.launch {
                    _events.send(CaptureUiEvent.ShowSnackbar(mapToCaptureError(intent.error).userMessage))
                }
            }
        }
    }

    private fun mapToCaptureError(e: Throwable): AnalysisError = when {
        e is IllegalStateException && e.message?.contains("준비") == true -> AnalysisError.CAMERA_NOT_READY
        else -> AnalysisError.INVALID_IMAGE
    }

    private fun mapToAnalysisError(e: Throwable): AnalysisError = when (e) {
        is UnknownHostException -> AnalysisError.NETWORK_UNAVAILABLE
        is SocketTimeoutException -> AnalysisError.TIMEOUT
        is kotlinx.coroutines.TimeoutCancellationException -> AnalysisError.TIMEOUT
        else -> AnalysisError.UNKNOWN
    }
}
