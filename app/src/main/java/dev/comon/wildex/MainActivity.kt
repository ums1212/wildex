package dev.comon.wildex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.comon.wildex.audio.BgmManager
import dev.comon.wildex.audio.LocalBgmManager
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.data.ThemePreferencesRepository
import dev.comon.wildex.navigation.WildexRoot
import dev.comon.wildex.ui.theme.WildexTheme

class MainActivity : ComponentActivity() {
    private val bgmManager = BgmManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> bgmManager.pause()
                Lifecycle.Event.ON_START -> bgmManager.resume()
                else -> Unit
            }
        })

        val themeRepo = ThemePreferencesRepository(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkOverride by themeRepo.darkThemeOverride.collectAsStateWithLifecycle(
                initialValue = null,
            )
            val useDarkTheme = darkOverride ?: systemDark
            val bgmEnabled by themeRepo.bgmEnabled.collectAsStateWithLifecycle(initialValue = true)

            LaunchedEffect(bgmEnabled, useDarkTheme) {
                if (bgmEnabled) {
                    bgmManager.switchTheme(applicationContext, useDarkTheme)
                    bgmManager.play(applicationContext, useDarkTheme)
                } else {
                    bgmManager.stop()
                }
            }

            CompositionLocalProvider(
                LocalThemePreferencesRepository provides themeRepo,
                LocalBgmManager provides bgmManager,
            ) {
                WildexTheme(darkTheme = useDarkTheme) {
                    WildexRoot(isDarkTheme = useDarkTheme)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bgmManager.stop()
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    WildexTheme {
        WildexRoot(isDarkTheme = false)
    }
}
