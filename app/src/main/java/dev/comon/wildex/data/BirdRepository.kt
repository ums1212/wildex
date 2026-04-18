package dev.comon.wildex.data

import android.content.Context
import dev.comon.wildex.domain.model.BirdDetail
import dev.comon.wildex.domain.model.BirdListResult
import dev.comon.wildex.network.WildexApiClient
import dev.comon.wildex.network.toDomain

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
     * 조류 이름으로 조류 상세 정보를 조회한다.
     * 1. `bird_info_*` 캐시에서 이름 검색 → 히트 시 즉시 반환
     * 2. `bird_list_page_*` 캐시에서 speciesId 검색 → 히트 시 [getBirdInfo] 호출
     * 3. 두 캐시 모두 미스 → [NoSuchElementException] 발생 (서버 검색 API 미구현 — 추후 연결 예정)
     */
    suspend fun searchByName(name: String): BirdDetail {
        cache.searchByName(name)?.let { return it }

        val speciesId = cache.findSpeciesIdByName(name)
            ?: throw NoSuchElementException(name)

        return getBirdInfo(speciesId)
    }
}
