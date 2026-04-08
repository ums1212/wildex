package dev.comon.wildex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.data.ThemePreferencesRepository
import dev.comon.wildex.ui.MainMenuScreen
import dev.comon.wildex.ui.TitleScreen
import dev.comon.wildex.ui.theme.WildexTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val RouteMainMenu = "main_menu"
private const val RouteTitle = "title"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepo = ThemePreferencesRepository(applicationContext)

        // DataStore에서 테마 값을 받을 때까지 스플래시 화면 유지
        var isThemeLoaded = false
        lifecycleScope.launch {
            themeRepo.darkThemeOverride.first()
            isThemeLoaded = true
        }
        splashScreen.setKeepOnScreenCondition { !isThemeLoaded }

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

@Composable
private fun WildexRoot(isDarkTheme: Boolean) {
    var route by rememberSaveable { mutableStateOf(RouteTitle) }
    var isLoggedIn by rememberSaveable { mutableStateOf(true) }

    when (route) {
        RouteMainMenu -> MainMenuScreen(
            isLoggedIn = isLoggedIn,
            userNickname = "TRAINER_L_10",
            onLoginClick = { route = RouteTitle },
            onLogout = { isLoggedIn = false },
        )
        else -> TitleScreen(
            userNickname = "TRAINER_L_10",
            isDarkTheme = isDarkTheme,
            onLoginClick = {
                isLoggedIn = true
                route = RouteMainMenu
            },
            onGuideClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    WildexTheme {
        WildexRoot(isDarkTheme = false)
    }
}
