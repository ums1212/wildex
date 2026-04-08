package dev.comon.wildex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.comon.wildex.screen.GuideScreen
import dev.comon.wildex.screen.MainMenuScreen
import dev.comon.wildex.screen.TitleScreen
import kotlinx.serialization.Serializable

// ── 루트 네비게이션 라우트 ────────────────────────────────────────────
@Serializable
data object WildexTitleRoute

@Serializable
data object WildexGuideRoute

/** MainMenuScreen 셸 전체를 가리키는 루트 레벨 라우트. */
@Serializable
data object WildexMainShellRoute

@Composable
fun WildexRoot(isDarkTheme: Boolean) {
    val navController = rememberNavController()
    var isLoggedIn by rememberSaveable { mutableStateOf(true) }

    NavHost(
        navController = navController,
        startDestination = WildexTitleRoute,
    ) {
        composable<WildexTitleRoute> {
            TitleScreen(
                userNickname = "TRAINER_L_10",
                isDarkTheme = isDarkTheme,
                onLoginClick = {
                    isLoggedIn = true
                    navController.navigate(WildexMainShellRoute) {
                        popUpTo(WildexTitleRoute) { inclusive = true }
                    }
                },
                onGuideClick = { navController.navigate(WildexGuideRoute) },
            )
        }
        composable<WildexGuideRoute> {
            GuideScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<WildexMainShellRoute> {
            MainMenuScreen(
                isLoggedIn = isLoggedIn,
                userNickname = "TRAINER_L_10",
                onLoginClick = {
                    navController.navigate(WildexTitleRoute) {
                        popUpTo(WildexMainShellRoute) { inclusive = true }
                    }
                },
                onLogout = { isLoggedIn = false },
            )
        }
    }
}
