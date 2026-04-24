package dev.comon.wildex.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaptureRecordRepository(application)

    val records: Flow<PagingData<CaptureRecordEntity>> =
        repository.recordsPager(pageSize = 10).cachedIn(viewModelScope)

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    fun enterEditMode(initialId: Long) {
        _selectedIds.value = setOf(initialId)
        _isEditMode.value = true
    }

    fun exitEditMode() {
        _isEditMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelected(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) current - id else current + id
    }

    fun deleteSelected(onDone: () -> Unit = {}) {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _isDeleting.value = true
            repository.deleteRecords(ids)
            _isDeleting.value = false
            _isEditMode.value = false
            _selectedIds.value = emptySet()
            onDone()
        }
    }
}
