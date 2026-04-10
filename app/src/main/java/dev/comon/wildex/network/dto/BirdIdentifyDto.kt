package dev.comon.wildex.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BirdIdentifyResponseDto(
    val success: Boolean,
    val predictions: List<BirdIdentifyPredictionDto> = emptyList(),
)

@Serializable
data class BirdIdentifyPredictionDto(
    val taxon: BirdIdentifyTaxonDto,
    @SerialName("combined_score") val combinedScore: Double = 0.0,
    @SerialName("vision_score") val visionScore: Double = 0.0,
)

@Serializable
data class BirdIdentifyTaxonDto(
    val id: Int,
    val name: String,
    val rank: String? = null,
    @SerialName("preferred_common_name") val preferredCommonName: String? = null,
)
