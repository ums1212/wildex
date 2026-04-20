package dev.comon.wildex.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.data.capture.CaptureRecordRepository
import kotlinx.coroutines.flow.Flow

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaptureRecordRepository(application)
    val records: Flow<PagingData<CaptureRecordEntity>> =
        repository.recordsPager(pageSize = 10).cachedIn(viewModelScope)
}
