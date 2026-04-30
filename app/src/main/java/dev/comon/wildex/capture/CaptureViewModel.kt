package dev.comon.wildex.capture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.BirdImageAnalyzer
import dev.comon.wildex.data.BirdRepository
import dev.comon.wildex.data.GeminiBirdImageAnalyzer
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    /** 분석 코루틴 핸들 — [CaptureIntent.CancelAnalysisClicked] 시 취소에 사용 */
    private var analysisJob: Job? = null

    fun onIntent(intent: CaptureIntent) {
        when (intent) {
            is CaptureIntent.PermissionResult ->
                _state.update { it.copy(hasCameraPermission = intent.granted) }

            is CaptureIntent.LocationPermissionResult ->
                _state.update { it.copy(hasLocationPermission = intent.granted) }

            CaptureIntent.ToggleFlash -> _state.update { it.copy(flashOn = !it.flashOn) }
            CaptureIntent.ToggleCaptureMode -> {
                val newMode = if (_state.value.captureMode == CaptureMode.Scan) CaptureMode.Record else CaptureMode.Scan
                _state.update { it.copy(captureMode = newMode) }
                viewModelScope.launch {
                    val message = if (newMode == CaptureMode.Scan) "스캔 모드로 전환합니다." else "기록 모드로 전환합니다."
                    _events.send(CaptureUiEvent.ShowSnackbar(message))
                }
            }

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
                // 촬영 진행 중이거나 분석/저장 중이면 연타 무시
                if (isCaptureInProgress || _state.value.isAnalyzing || _state.value.isSaving) return
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
                when (_state.value.captureMode) {
                    CaptureMode.Scan -> runScanFlow(intent.imageBytes, intent.rotationDegrees)
                    CaptureMode.Record -> runRecordFlow(intent.imageBytes, intent.rotationDegrees)
                }
            }

            CaptureIntent.CancelAnalysisClicked -> {
                if (_state.value.isAnalyzing) {
                    analysisJob?.cancel()
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

    private fun runScanFlow(imageBytes: ByteArray, rotationDegrees: Int) {
        analysisJob = viewModelScope.launch {
            val hasLocationPerm = _state.value.hasLocationPermission
            // NonCancellable: 사용자 취소 후에도 finally 에서 deleteRecord 를 호출하기 위해 recordId 확보
            val saveDeferred = async(NonCancellable) {
                val uri = captureRecordRepository.saveImageToMediaStore(imageBytes, rotationDegrees)
                uri?.let { captureRecordRepository.insertCaptureRecord(it, hasLocationPerm) }
            }
            var completedTerminally = false
            try {
                birdImageAnalyzer.recognizeBird(imageBytes, rotationDegrees)
                    .catch { e -> emit(BirdRecognitionState.Error(mapToRecognitionError(e))) }
                    .collect { recognitionState ->
                        when (recognitionState) {
                            BirdRecognitionState.Analyzing -> {
                                _state.update {
                                    it.copy(
                                        isAnalyzing = true,
                                        frozenFrameBytes = imageBytes,
                                        frozenFrameRotation = rotationDegrees,
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
                                completedTerminally = true
                                searchBirdAndNavigate(name, recordId)
                            }
                            is BirdRecognitionState.Error -> {
                                completedTerminally = true
                                _state.update { it.copy(isAnalyzing = false, frozenFrameBytes = null) }
                                _events.send(CaptureUiEvent.ShowSnackbar(recognitionState.error.userMessage))
                            }
                        }
                    }
            } finally {
                // 사용자 취소 시에만 실행 (Recognized/Error 는 completedTerminally = true)
                if (!completedTerminally) {
                    withContext(NonCancellable) {
                        val recordId = saveDeferred.await()
                        if (recordId != null) captureRecordRepository.deleteRecord(recordId)
                        _state.update {
                            it.copy(isAnalyzing = false, frozenFrameBytes = null, frozenFrameRotation = 0)
                        }
                        _events.send(CaptureUiEvent.ShowSnackbar("분석을 취소했습니다"))
                    }
                }
            }
        }
    }

    private fun runRecordFlow(imageBytes: ByteArray, rotationDegrees: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val uri = captureRecordRepository.saveImageToMediaStore(imageBytes, rotationDegrees)
                if (uri == null) {
                    _events.send(CaptureUiEvent.ShowSnackbar(BirdRecognitionError.UNKNOWN.userMessage))
                    return@launch
                }
                captureRecordRepository.insertCaptureRecord(uri, _state.value.hasLocationPermission)
                _events.send(CaptureUiEvent.ShowSnackbar("기록을 저장했습니다"))
            } catch (e: Throwable) {
                _events.send(CaptureUiEvent.ShowSnackbar(BirdRecognitionError.UNKNOWN.userMessage))
            } finally {
                _state.update { it.copy(isSaving = false) }
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
