package dev.comon.wildex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.data.ThemePreferencesRepository
import dev.comon.wildex.navigation.WildexRoot
import dev.comon.wildex.ui.theme.WildexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepo = ThemePreferencesRepository(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkOverride by themeRepo.darkThemeOverride.collectAsStateWithLifecycle(
                initialValue = null,
            )
            val useDarkTheme = darkOverride ?: systemDark

            CompositionLocalProvider(
                LocalThemePreferencesRepository provides themeRepo,
            ) {
                WildexTheme(darkTheme = useDarkTheme) {
                    WildexRoot(isDarkTheme = useDarkTheme)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    WildexTheme {
        WildexRoot(isDarkTheme = false)
    }
}
