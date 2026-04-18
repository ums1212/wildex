package dev.comon.wildex.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BirdSearchResponseDto(
    val success: Boolean = false,
    val results: List<BirdSearchResultDto> = emptyList(),
)

@Serializable
data class BirdSearchResultDto(
    @SerialName("anml_nm") val anmlNm: String = "",
    val anmlSpecsId: String = "",
)
