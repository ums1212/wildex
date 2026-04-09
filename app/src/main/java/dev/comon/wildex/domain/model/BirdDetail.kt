package dev.comon.wildex.domain.model

data class BirdDetail(
    val speciesId: String,
    val specimenNo: String,
    val name: String,
    val scientificName: String,
    val phylumEngName: String,
    val phylumName: String,
    val classEngName: String,
    val className: String,
    val orderEngName: String,
    val orderName: String,
    val familyEngName: String,
    val familyName: String,
    val genusEngName: String,
    val genusName: String,
    val generalFeature: String,
    val ecologicalFeature: String,
    val imageUrl: String,
    val copyright: String
)
