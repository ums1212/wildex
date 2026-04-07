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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.data.ThemePreferencesRepository
import dev.comon.wildex.ui.MainMenuScreen
import dev.comon.wildex.ui.TitleScreen
import dev.comon.wildex.ui.theme.WildexTheme

private const val RouteMainMenu = "main_menu"
private const val RouteTitle = "title"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferencesRepository = remember {
                ThemePreferencesRepository(applicationContext)
            }
            val systemDark = isSystemInDarkTheme()
            val darkOverride by themePreferencesRepository.darkThemeOverride.collectAsStateWithLifecycle(
                initialValue = null,
            )
            val useDarkTheme = darkOverride ?: systemDark

            CompositionLocalProvider(
                LocalThemePreferencesRepository provides themePreferencesRepository,
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