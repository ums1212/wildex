package dev.comon.wildex.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordDetailViewModel(
    application: Application,
    private val recordId: Long,
) : AndroidViewModel(application) {
    private val repository = CaptureRecordRepository(application)
    val record: StateFlow<CaptureRecordEntity?> =
        repository.observeRecord(recordId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            onDeleted()
        }
    }
}
