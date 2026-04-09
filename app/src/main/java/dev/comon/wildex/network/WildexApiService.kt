package dev.comon.wildex.network

import dev.comon.wildex.network.dto.BirdInfoResponseDto
import dev.comon.wildex.network.dto.BirdListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WildexApiService {

    @GET("/api/wildex/bird-list/")
    suspend fun getBirdList(
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1
    ): BirdListResponseDto

    @GET("/api/wildex/bird-info/")
    suspend fun getBirdInfo(
        @Query("q1") anmlSpecsId: String
    ): BirdInfoResponseDto
}
