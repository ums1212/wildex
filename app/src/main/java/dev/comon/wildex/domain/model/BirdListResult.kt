package dev.comon.wildex.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BirdListResult(
    val items: List<BirdSummary>,
    val totalCount: Int,
    val pageNo: Int,
    val numOfRows: Int
)
