package dev.comon.wildex.capture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.BirdImageAnalyzer
import dev.comon.wildex.data.BirdRepository
import dev.comon.wildex.data.GeminiBirdImageAnalyzer
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.async
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

class CaptureViewModel(application: Application) : AndroidViewModel(application) {

    private val birdImageAnalyzer: BirdImageAnalyzer = GeminiBirdImageAnalyzer()
    private val birdRepository: BirdRepository = BirdRepository(application)
    private val captureRecordRepository: CaptureRecordRepository = CaptureRecordRepository(application)

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

            is CaptureIntent.LocationPermissionResult ->
                _state.update { it.copy(hasLocationPermission = intent.granted) }

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
                        BirdRecognitionError.UNKNOWN.userMessage,
                    ),
                )
            }

            is CaptureIntent.CaptureSucceeded -> {
                isCaptureInProgress = false
                viewModelScope.launch {
                    val hasLocationPerm = _state.value.hasLocationPermission
                    val saveDeferred = async {
                        captureRecordRepository.saveCapture(
                            imageBytes = intent.imageBytes,
                            rotationDegrees = intent.rotationDegrees,
                            hasLocationPermission = hasLocationPerm,
                        )
                    }
                    birdImageAnalyzer.recognizeBird(intent.imageBytes, intent.rotationDegrees)
                        .catch { e ->
                            emit(BirdRecognitionState.Error(mapToRecognitionError(e)))
                        }
                        .collect { recognitionState ->
                            when (recognitionState) {
                                BirdRecognitionState.Analyzing -> {
                                    _state.update {
                                        it.copy(
                                            isAnalyzing = true,
                                            frozenFrameBytes = intent.imageBytes,
                                            frozenFrameRotation = intent.rotationDegrees,
                                        )
                                    }
                                }
                                is BirdRecognitionState.Recognized -> {
                                    val name = recognitionState.birdName
                                    val category = recognitionState.category
                                    val recordId = saveDeferred.await()
                                    recordId?.let { id ->
                                        captureRecordRepository.updateRecognition(id, name, category)
                                    }
                                    searchBirdAndNavigate(name, recordId)
                                }
                                is BirdRecognitionState.Error -> {
                                    _state.update { it.copy(isAnalyzing = false, frozenFrameBytes = null) }
                                    _events.send(
                                        CaptureUiEvent.ShowSnackbar(recognitionState.error.userMessage),
                                    )
                                }
                            }
                        }
                }
            }

            is CaptureIntent.CaptureFailed -> {
                isCaptureInProgress = false
                viewModelScope.launch {
                    _events.send(
                        CaptureUiEvent.ShowSnackbar(BirdRecognitionError.UNKNOWN.userMessage),
                    )
                }
            }
        }
    }

    /** 조류 이름 인식 후 DataStore → 검색 API 순으로 speciesId를 조회하여 화면 이동 */
    private suspend fun searchBirdAndNavigate(birdName: String, recordId: Long?) {
        runCatching { birdRepository.searchByName(birdName) }
            .onSuccess { speciesId ->
                _state.update { it.copy(isAnalyzing = false, frozenFrameBytes = null) }
                _events.send(CaptureUiEvent.NavigateToBirdInfo(speciesId, recordId))
            }
            .onFailure {
                _state.update { it.copy(isAnalyzing = false, frozenFrameBytes = null) }
                _events.send(
                    CaptureUiEvent.ShowSnackbar("조류 정보를 찾을 수 없습니다. 다시 시도해주세요."),
                )
            }
    }

    private fun mapToRecognitionError(e: Throwable): BirdRecognitionError = when (e) {
        is UnknownHostException -> BirdRecognitionError.NETWORK_UNAVAILABLE
        is SocketTimeoutException -> BirdRecognitionError.TIMEOUT
        is kotlinx.coroutines.TimeoutCancellationException -> BirdRecognitionError.TIMEOUT
        else -> BirdRecognitionError.UNKNOWN
    }
}
