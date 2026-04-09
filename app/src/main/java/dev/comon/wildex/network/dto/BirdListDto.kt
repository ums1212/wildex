package dev.comon.wildex.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BirdListResponseDto(
    val response: BirdListOuterDto?
)

@Serializable
data class BirdListOuterDto(
    val header: ApiHeaderDto?,
    val body: BirdListBodyDto?
)

@Serializable
data class BirdListBodyDto(
    val items: BirdListItemsDto?,
    val numOfRows: String?,
    val pageNo: String?,
    val totalCount: String?
)

@Serializable
data class BirdListItemsDto(
    val item: List<BirdListItemDto>?
)

@Serializable
data class BirdListItemDto(
    val anmlGnrlNm: String?,
    val anmlScnm: String?,
    val anmlSpecsId: String?,
    val clsscSstemSctinKrlngNm: String?,
    val cprtCtnt: String?
)
