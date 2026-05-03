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
    private val _nameInput = MutableStateFlow<String?>(null)
    val nameInput: StateFlow<String?> = _nameInput
    private val _categoryInput = MutableStateFlow<String?>(null)
    val categoryInput: StateFlow<String?> = _categoryInput
    private val _addressInput = MutableStateFlow<String?>(null)
    val addressInput: StateFlow<String?> = _addressInput

    init {
        viewModelScope.launch {
            record.filterNotNull().collect { rec ->
                if (_memoInput.value == null) _memoInput.value = rec.memo ?: ""
                if (_nameInput.value == null) _nameInput.value = rec.name ?: ""
                if (_categoryInput.value == null) _categoryInput.value = rec.category ?: ""
                if (_addressInput.value == null) _addressInput.value = rec.address
            }
        }
        viewModelScope.launch {
            _memoInput.filterNotNull().debounce(500).distinctUntilChanged()
                .collect { repository.updateMemo(recordId, it) }
        }
        viewModelScope.launch {
            _nameInput.filterNotNull().debounce(500).distinctUntilChanged()
                .collect { repository.updateName(recordId, it) }
        }
        viewModelScope.launch {
            _categoryInput.filterNotNull().debounce(500).distinctUntilChanged()
                .collect { repository.updateCategory(recordId, it) }
        }
        viewModelScope.launch {
            _addressInput.filterNotNull().debounce(500).distinctUntilChanged()
                .collect { repository.updateAddress(recordId, it) }
        }
    }

    fun onMemoChange(text: String) {
        _memoInput.value = text.take(500)
    }

    fun onNameChange(text: String) {
        _nameInput.value = text.take(50)
    }

    fun onCategoryChange(text: String) {
        _categoryInput.value = text.take(30)
    }

    fun onAddressChange(text: String) {
        _addressInput.value = text.take(100)
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            onDeleted()
        }
    }
}
