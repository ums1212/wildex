package dev.comon.wildex.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

private data class RecordsQueryParams(
    val ascending: Boolean,
    val startMillis: Long?,
    val endMillis: Long?,
    val category: RecordsSearchCategory,
    val query: String?,
)

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaptureRecordRepository(application)

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _dateFilterStartMillis = MutableStateFlow<Long?>(null)
    val dateFilterStartMillis: StateFlow<Long?> = _dateFilterStartMillis.asStateFlow()

    private val _dateFilterEndMillis = MutableStateFlow<Long?>(null)
    val dateFilterEndMillis: StateFlow<Long?> = _dateFilterEndMillis.asStateFlow()

    private val _searchCategory = MutableStateFlow(RecordsSearchCategory.NAME)
    val searchCategory: StateFlow<RecordsSearchCategory> = _searchCategory.asStateFlow()

    private val _submittedQuery = MutableStateFlow<String?>(null)

    private val _pendingQuery = MutableStateFlow("")
    val pendingQuery: StateFlow<String> = _pendingQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val records: Flow<PagingData<CaptureRecordEntity>> =
        combine(
            _sortAscending,
            _dateFilterStartMillis,
            _dateFilterEndMillis,
            _searchCategory,
            _submittedQuery,
        ) { asc, startMs, endMs, cat, q -> RecordsQueryParams(asc, startMs, endMs, cat, q) }
            .flatMapLatest { p ->
                repository.filteredRecordsPager(
                    pageSize = 10,
                    startMillis = p.startMillis,
                    endMillis = p.endMillis,
                    category = p.category.name,
                    query = p.query?.takeIf { it.isNotBlank() },
                    ascending = p.ascending,
                )
            }
            .cachedIn(viewModelScope)

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    fun toggleSort() {
        _sortAscending.value = !_sortAscending.value
    }

    fun setDateFilter(startMillis: Long?, endMillis: Long?) {
        _dateFilterStartMillis.value = startMillis
        _dateFilterEndMillis.value = endMillis
    }

    fun setSearchCategory(c: RecordsSearchCategory) {
        _searchCategory.value = c
    }

    fun onPendingQueryChange(s: String) {
        _pendingQuery.value = s
    }

    fun enterSearchMode() {
        exitEditMode()
        _isSearchMode.value = true
    }

    fun exitSearchMode() {
        _isSearchMode.value = false
        _pendingQuery.value = ""
        _submittedQuery.value = null
    }

    fun submitSearch() {
        _submittedQuery.value = _pendingQuery.value.trim().ifEmpty { null }
    }

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

