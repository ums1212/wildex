package dev.comon.wildex.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BirdSummary(
    val speciesId: String,
    val name: String,
    val scientificName: String,
    val familyName: String,
    val copyright: String
)
