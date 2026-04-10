package dev.comon.wildex.domain.model

data class BirdPrediction(
    val taxonId: Int,
    val scientificName: String,
    val commonName: String?,
    val combinedScore: Double,
    val visionScore: Double,
)
