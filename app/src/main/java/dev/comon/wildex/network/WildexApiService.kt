package dev.comon.wildex.network

import dev.comon.wildex.network.dto.BirdIdentifyResponseDto
import dev.comon.wildex.network.dto.BirdInfoResponseDto
import dev.comon.wildex.network.dto.BirdListResponseDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @Multipart
    @POST("/api/wildex/bird-identify/")
    suspend fun identifyBird(
        @Part image: MultipartBody.Part
    ): BirdIdentifyResponseDto

    /** 조류 이름으로 조류 정보를 검색한다. (서버 API 미구현 — placeholder) */
    @GET("/api/wildex/bird-search/")
    suspend fun searchBirdByName(
        @Query("name") name: String
    ): BirdInfoResponseDto
}
