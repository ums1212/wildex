package dev.comon.wildex.data

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wildexPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wildex_preferences",
)

private val DarkThemeKey = booleanPreferencesKey("dark_theme")

/**
 * 저장된 값이 없으면 [darkThemeOverride]는 null이며, UI에서 시스템 다크 모드를 따르면 됩니다.
 * 사용자가 스위치를 바꾸면 true/false가 저장됩니다.
 */
class ThemePreferencesRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.wildexPreferencesDataStore

    val darkThemeOverride: Flow<Boolean?> = dataStore.data.map { prefs ->
        if (prefs.contains(DarkThemeKey)) prefs[DarkThemeKey] else null
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DarkThemeKey] = enabled
        }
    }
}

val LocalThemePreferencesRepository = staticCompositionLocalOf<ThemePreferencesRepository> {
    error("ThemePreferencesRepository가 제공되지 않았습니다.")
}
