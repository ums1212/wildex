package dev.comon.wildex.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.Properties

class WildexApiServiceTest {

    private lateinit var service: WildexApiService

    @Before
    fun setUp() {
        val localProperties = Properties().apply {
            val file = File("../local.properties")
            if (file.exists()) load(file.inputStream())
        }
        val apiHost = localProperties["WILDEX_API_HOST"] as String
        val apiKey = localProperties["WILDEX_API_KEY"] as String

        val loggingInterceptor = HttpLoggingInterceptor { println("[HTTP] $it") }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Wildex-Api-Key", apiKey)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        service = Retrofit.Builder()
            .baseUrl(apiHost)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WildexApiService::class.java)
    }

    @Test
    fun `getBirdList - 목록을 정상적으로 가져온다`() = runTest {
        val response = service.getBirdList(numOfRows = 10, pageNo = 1)

        assertEquals("00", response.response?.header?.resultCode)

        val body = response.response?.body
        assertNotNull(body)
        assertEquals("10", body?.numOfRows)
        assertEquals("1", body?.pageNo)

        val items = body?.items?.item.orEmpty()
        assertFalse("아이템 목록이 비어있습니다", items.isEmpty())
        items.forEach { item ->
            assertFalse(item.anmlSpecsId.orEmpty().isEmpty())
            assertFalse(item.anmlGnrlNm.orEmpty().isEmpty())
        }
    }

    @Test
    fun `getBirdList - toDomain 변환이 올바르게 동작한다`() = runTest {
        val result = service.getBirdList(numOfRows = 5, pageNo = 1).toDomain()

        assertFalse(result.items.isEmpty())
        assertEquals(5, result.numOfRows)
        assertEquals(1, result.pageNo)
        result.items.forEach { bird ->
            assertFalse(bird.speciesId.isEmpty())
            assertFalse(bird.name.isEmpty())
        }
    }

    @Test
    fun `getBirdInfo - 상세정보를 정상적으로 가져온다`() = runTest {
        val response = service.getBirdInfo(anmlSpecsId = "A000001305")

        assertEquals("00", response.response?.header?.resultCode)

        val item = response.response?.body?.item
        assertNotNull(item)
        assertEquals("A000001305", item?.anmlSpecsId)
        assertFalse(item?.anmlGnrlNm.orEmpty().isEmpty())
        assertNotNull(item?.imgUrl)
    }

    @Test
    fun `getBirdInfo - toDomain 변환이 올바르게 동작한다`() = runTest {
        val result = service.getBirdInfo(anmlSpecsId = "A000001305").toDomain()

        assertEquals("A000001305", result.speciesId)
        assertFalse(result.name.isEmpty())
        assertFalse(result.className.isEmpty())
        assertFalse(result.orderName.isEmpty())
        assertFalse(result.familyName.isEmpty())
    }
}
