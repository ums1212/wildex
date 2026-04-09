package dev.comon.wildex.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BirdInfoResponseDto(
    val response: BirdInfoOuterDto?
)

@Serializable
data class BirdInfoOuterDto(
    val header: ApiHeaderDto?,
    val body: BirdInfoBodyDto?
)

@Serializable
data class BirdInfoBodyDto(
    val item: BirdInfoItemDto?
)

@Serializable
data class BirdInfoItemDto(
    val anmlClsEngNm: String?,
    val anmlClsKorNm: String?,
    val anmlFmlyEngNm: String?,
    val anmlFmlyKorNm: String?,
    val anmlGenusEngNm: String?,
    val anmlGenusKorNm: String?,
    val anmlGnrlNm: String?,
    val anmlOrdEngNm: String?,
    val anmlOrdKorNm: String?,
    val anmlPhlmEngNm: String?,
    val anmlPhlmKorNm: String?,
    val anmlScnm: String?,
    val anmlSmplNo: String?,
    val anmlSpecsId: String?,
    val eclgDpftrCont: String?,
    val gnrlSpftrCont: String?,
    val imgUrl: String?,
    val cprtCtnt: String?
)
