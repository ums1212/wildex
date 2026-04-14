package dev.comon.wildex.journal.birdinfo

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.util.Locale

class BirdInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BirdRepository(application)

    private val _state = MutableStateFlow(BirdInfoUiState())
    val state: StateFlow<BirdInfoUiState> = _state.asStateFlow()

    private val _events = Channel<BirdInfoUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var tts: TextToSpeech? = null
    @Volatile private var ttsReady = false

    fun onIntent(intent: BirdInfoIntent) {
        when (intent) {
            is BirdInfoIntent.Load -> loadInfo(intent.speciesId)
            is BirdInfoIntent.Retry -> loadInfo(intent.speciesId)
            BirdInfoIntent.ToggleTts -> toggleTts()
            BirdInfoIntent.StopTts -> stopTts()
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
        initTtsIfNeeded()
    }

    private fun initTtsIfNeeded() {
        if (tts != null) return
        tts = TextToSpeech(getApplication()) { status ->
            if (status != TextToSpeech.SUCCESS) {
                viewModelScope.launch {
                    _events.send(BirdInfoUiEvent.ShowTtsError("현재 TTS를 재생할 수 없습니다."))
                }
                return@TextToSpeech
            }
            val locale = Locale.getDefault()
            val langResult = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallback = tts?.setLanguage(Locale.ENGLISH)
                if (fallback == TextToSpeech.LANG_MISSING_DATA || fallback == TextToSpeech.LANG_NOT_SUPPORTED) {
                    viewModelScope.launch {
                        _events.send(BirdInfoUiEvent.ShowTtsError("현재 TTS를 재생할 수 없습니다."))
                    }
                    return@TextToSpeech
                }
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "birdinfo_tts") {
                        viewModelScope.launch {
                            _state.update { it.copy(isSpeaking = false) }
                            _events.send(BirdInfoUiEvent.RestoreBgm)
                        }
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    viewModelScope.launch {
                        _state.update { it.copy(isSpeaking = false) }
                        _events.send(BirdInfoUiEvent.RestoreBgm)
                        _events.send(BirdInfoUiEvent.ShowTtsError("현재 TTS를 재생할 수 없습니다."))
                    }
                }
            })
            ttsReady = true
        }
    }

    private fun toggleTts() {
        if (_state.value.isSpeaking) {
            tts?.stop()
            _state.update { it.copy(isSpeaking = false) }
            viewModelScope.launch { _events.send(BirdInfoUiEvent.RestoreBgm) }
            return
        }
        if (!ttsReady) {
            viewModelScope.launch {
                _events.send(BirdInfoUiEvent.ShowTtsError("현재 TTS를 재생할 수 없습니다."))
            }
            return
        }
        val bird = _state.value.bird ?: return
        _state.update { it.copy(isSpeaking = true) }
        viewModelScope.launch { _events.send(BirdInfoUiEvent.DuckBgm) }
        tts?.speak(bird.name, TextToSpeech.QUEUE_FLUSH, null, "birdinfo_tts_name")
        tts?.speak(bird.generalFeature, TextToSpeech.QUEUE_ADD, null, "birdinfo_tts")
    }

    private fun stopTts() {
        if (!_state.value.isSpeaking) return
        tts?.stop()
        _state.update { it.copy(isSpeaking = false) }
        viewModelScope.launch { _events.send(BirdInfoUiEvent.RestoreBgm) }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
