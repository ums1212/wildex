package dev.comon.wildex.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class RecordDetailViewModel(
    application: Application,
    private val recordId: Long,
) : AndroidViewModel(application) {
    private val repository = CaptureRecordRepository(application)
    val record: StateFlow<CaptureRecordEntity?> =
        repository.observeRecord(recordId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _memoInput = MutableStateFlow<String?>(null)
    val memoInput: StateFlow<String?> = _memoInput

    init {
        viewModelScope.launch {
            record.filterNotNull().collect { rec ->
                if (_memoInput.value == null) {
                    _memoInput.value = rec.memo ?: ""
                }
            }
        }
        viewModelScope.launch {
            _memoInput
                .filterNotNull()
                .debounce(500)
                .distinctUntilChanged()
                .collect { memo ->
                    repository.updateMemo(recordId, memo)
                }
        }
    }

    fun onMemoChange(text: String) {
        _memoInput.value = text.take(500)
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            onDeleted()
        }
    }
}
