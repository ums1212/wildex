package dev.comon.wildex.network

import dev.comon.wildex.domain.model.BirdDetail
import dev.comon.wildex.domain.model.BirdListResult
import dev.comon.wildex.domain.model.BirdSummary
import dev.comon.wildex.network.dto.BirdInfoResponseDto
import dev.comon.wildex.network.dto.BirdListResponseDto

fun BirdListResponseDto.toDomain(): BirdListResult {
    val body = response?.body
    return BirdListResult(
        items = body?.items?.item.orEmpty().map { item ->
            BirdSummary(
                speciesId = item.anmlSpecsId.orEmpty(),
                name = item.anmlGnrlNm.orEmpty(),
                scientificName = item.anmlScnm.orEmpty(),
                familyName = item.clsscSstemSctinKrlngNm.orEmpty(),
                copyright = item.cprtCtnt.orEmpty()
            )
        },
        totalCount = body?.totalCount?.toIntOrNull() ?: 0,
        pageNo = body?.pageNo?.toIntOrNull() ?: 1,
        numOfRows = body?.numOfRows?.toIntOrNull() ?: 10
    )
}

fun BirdInfoResponseDto.toDomain(): BirdDetail {
    val item = response?.body?.item
    return BirdDetail(
        speciesId = item?.anmlSpecsId.orEmpty(),
        specimenNo = item?.anmlSmplNo.orEmpty(),
        name = item?.anmlGnrlNm.orEmpty(),
        scientificName = item?.anmlScnm.orEmpty(),
        phylumEngName = item?.anmlPhlmEngNm.orEmpty(),
        phylumName = item?.anmlPhlmKorNm.orEmpty(),
        classEngName = item?.anmlClsEngNm.orEmpty(),
        className = item?.anmlClsKorNm.orEmpty(),
        orderEngName = item?.anmlOrdEngNm.orEmpty(),
        orderName = item?.anmlOrdKorNm.orEmpty(),
        familyEngName = item?.anmlFmlyEngNm.orEmpty(),
        familyName = item?.anmlFmlyKorNm.orEmpty(),
        genusEngName = item?.anmlGenusEngNm.orEmpty(),
        genusName = item?.anmlGenusKorNm.orEmpty(),
        generalFeature = item?.gnrlSpftrCont.orEmpty(),
        ecologicalFeature = item?.eclgDpftrCont.orEmpty(),
        imageUrl = item?.imgUrl.orEmpty(),
        copyright = item?.cprtCtnt.orEmpty()
    )
}
