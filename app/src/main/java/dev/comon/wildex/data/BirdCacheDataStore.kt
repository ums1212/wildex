package dev.comon.wildex.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.comon.wildex.domain.model.BirdDetail
import dev.comon.wildex.domain.model.BirdListResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.birdCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wildex_bird_cache",
)

private val cacheJson = Json { ignoreUnknownKeys = true }

private fun birdListPageKey(page: Int) = stringPreferencesKey("bird_list_page_$page")
private fun birdInfoKey(speciesId: String) = stringPreferencesKey("bird_info_$speciesId")

class BirdCacheDataStore(context: Context) {

    private val store = context.applicationContext.birdCacheDataStore

    // ── BirdList ──────────────────────────────────────────────────────────

    suspend fun getBirdListPage(page: Int): BirdListResult? {
        val key = birdListPageKey(page)
        val json = store.data.map { it[key] }.firstOrNull() ?: return null
        return runCatching { cacheJson.decodeFromString<BirdListResult>(json) }.getOrNull()
    }

    suspend fun saveBirdListPage(page: Int, result: BirdListResult) {
        val key = birdListPageKey(page)
        val json = runCatching { cacheJson.encodeToString(BirdListResult.serializer(), result) }
            .getOrNull() ?: return
        store.edit { it[key] = json }
    }

    // ── BirdInfo ──────────────────────────────────────────────────────────

    suspend fun getBirdInfo(speciesId: String): BirdDetail? {
        val key = birdInfoKey(speciesId)
        val json = store.data.map { it[key] }.firstOrNull() ?: return null
        return runCatching { cacheJson.decodeFromString<BirdDetail>(json) }.getOrNull()
    }

    suspend fun saveBirdInfo(speciesId: String, detail: BirdDetail) {
        val key = birdInfoKey(speciesId)
        val json = runCatching { cacheJson.encodeToString(BirdDetail.serializer(), detail) }
            .getOrNull() ?: return
        store.edit { it[key] = json }
    }
}
