package dev.comon.wildex.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.comon.wildex.screen.GuideScreen
import dev.comon.wildex.screen.MainMenuScreen
import dev.comon.wildex.screen.SplashScreen
import dev.comon.wildex.screen.TitleScreen
import kotlinx.serialization.Serializable

// ── 루트 네비게이션 라우트 ────────────────────────────────────────────
@Serializable
data object WildexSplashRoute

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavHost(
            navController = navController,
            startDestination = WildexSplashRoute,
        ) {
            composable<WildexSplashRoute>(
                exitTransition = { fadeOut(tween(300, easing = FastOutSlowInEasing)) },
            ) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(WildexTitleRoute) {
                            popUpTo(WildexSplashRoute) { inclusive = true }
                        }
                    },
                )
            }
            composable<WildexTitleRoute>(
                enterTransition = {
                    if (initialState.destination.hasRoute(WildexSplashRoute::class)) {
                        fadeIn(tween(300, easing = FastOutSlowInEasing))
                    } else {
                        scaleIn(
                            initialScale = 0f,
                            animationSpec = tween(500, easing = FastOutSlowInEasing),
                        ) + fadeIn(tween(500, easing = FastOutSlowInEasing))
                    }
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
            ) {
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
            composable<WildexMainShellRoute>(
                enterTransition = { fadeIn(tween(350, easing = FastOutSlowInEasing)) },
                exitTransition = { fadeOut(tween(350, easing = FastOutSlowInEasing)) },
                popEnterTransition = { fadeIn(tween(350, easing = FastOutSlowInEasing)) },
                popExitTransition = { fadeOut(tween(350, easing = FastOutSlowInEasing)) },
            ) {
                MainMenuScreen(
                    animatedVisibilityScope = this,
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
}
