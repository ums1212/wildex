package dev.comon.wildex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
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
            WildexTheme {
                WildexRoot()
            }
        }
    }
}

@Composable
private fun WildexRoot() {
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
        WildexRoot()
    }
}