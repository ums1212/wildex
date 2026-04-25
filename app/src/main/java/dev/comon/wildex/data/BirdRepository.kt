package dev.comon.wildex.data

import android.content.Context
import dev.comon.wildex.domain.model.BirdDetail
import dev.comon.wildex.domain.model.BirdListResult
import dev.comon.wildex.domain.model.BirdSummary
import dev.comon.wildex.network.WildexApiClient
import dev.comon.wildex.network.dto.BirdSearchResultDto
import dev.comon.wildex.network.toDomain
import retrofit2.HttpException

/**
 * 캐시-우선(Cache-First) 전략:
 * - 캐시 히트 → 즉시 반환 (네트워크 호출 없음)
 * - 캐시 미스 → API 호출 → 결과를 캐시에 저장 후 반환
 */
class BirdRepository(context: Context) {

    private val api = WildexApiClient.wildexApiService
    private val cache = BirdCacheDataStore(context)

    suspend fun getBirdListPage(page: Int, numOfRows: Int): BirdListResult {
        cache.getBirdListPage(page)?.let { return it }

        val result = api.getBirdList(numOfRows = numOfRows, pageNo = page).toDomain()
        cache.saveBirdListPage(page, result)
        return result
    }

    suspend fun getBirdInfo(speciesId: String): BirdDetail {
        cache.getBirdInfo(speciesId)?.let { return it }

        val detail = api.getBirdInfo(anmlSpecsId = speciesId).toDomain()
        cache.saveBirdInfo(speciesId, detail)
        return detail
    }

    /**
     * 조류 이름으로 anmlSpecsId 를 반환한다.
     * 1. `bird_info_*` 캐시에서 이름 검색 → 히트 시 speciesId 반환
     * 2. `bird_list_page_*` 캐시에서 speciesId 역추적
     * 3. 서버 `/api/wildex/bird-search/` 호출 → 매칭된 anmlSpecsId 반환
     * 최종 미스 / 404: [NoSuchElementException] 발생
     */
    suspend fun searchByName(name: String): String {
        cache.searchByName(name)?.let { return it.speciesId }

        cache.findSpeciesIdByName(name)?.let { return it }

        return try {
            val response = api.searchBirdByName(name = name)
            pickBestMatch(response.results, name)?.anmlSpecsId
                ?: throw NoSuchElementException(name)
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw NoSuchElementException(name)
                400 -> throw IllegalArgumentException("Invalid anml_nm: '$name'", e)
                else -> throw e
            }
        }
    }

    /**
     * 조류 이름으로 BirdSummary 목록을 반환한다.
     * 1. `bird_list_page_*` 캐시에서 부분 일치 → 히트 시 즉시 반환
     * 2. 서버 `/api/wildex/bird-search-list/` 호출 → 결과 반환
     * 서버 404 → 빈 리스트, 400 → [IllegalArgumentException]
     */
    suspend fun searchListByName(name: String): List<BirdSummary> {
        val cached = cache.findAllByName(name)
        if (cached.isNotEmpty()) return cached

        return try {
            api.searchBirdListByName(name = name).toDomain().items
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emptyList()
                400 -> throw IllegalArgumentException("Invalid anml_nm: '$name'", e)
                else -> throw e
            }
        }
    }

    private fun pickBestMatch(
        results: List<BirdSearchResultDto>,
        query: String,
    ): BirdSearchResultDto? {
        if (results.isEmpty()) return null
        val q = query.trim()
        results.firstOrNull { it.anmlNm.trim().equals(q, ignoreCase = true) }
            ?.let { return it }
        val qNoSpace = q.replace(" ", "")
        results.firstOrNull {
            it.anmlNm.replace(" ", "").equals(qNoSpace, ignoreCase = true)
        }?.let { return it }
        return results.first()
    }
}
