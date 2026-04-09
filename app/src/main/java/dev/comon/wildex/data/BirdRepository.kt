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
}
