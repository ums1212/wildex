package dev.comon.wildex.domain.model

data class BirdListResult(
    val items: List<BirdSummary>,
    val totalCount: Int,
    val pageNo: Int,
    val numOfRows: Int
)
