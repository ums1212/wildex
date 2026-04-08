package dev.comon.wildex.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavOptionsBuilder
import kotlinx.serialization.Serializable

// ── MainMenuScreen 내부 NavHost 라우트 ────────────────────────────────
@Serializable
data object WildexMainMenuRoute

/** 메인 셸 하단 4탭에 해당하는 라우트만 묶는 마커(직렬화·목록 타입 공유용). */
@Serializable
sealed interface WildexMainBottomTabRoute

@Serializable
data object WildexJournalTabRoute : WildexMainBottomTabRoute

@Serializable
data object WildexCaptureTabRoute : WildexMainBottomTabRoute

@Serializable
data object WildexSearchTabRoute : WildexMainBottomTabRoute

@Serializable
data object WildexSettingsTabRoute : WildexMainBottomTabRoute

fun NavDestination?.wildexSelectedMainBottomTab(): WildexMainBottomTabRoute? {
    if (this == null) return null
    return when {
        hasRoute(WildexJournalTabRoute::class) -> WildexJournalTabRoute
        hasRoute(WildexCaptureTabRoute::class) -> WildexCaptureTabRoute
        hasRoute(WildexSearchTabRoute::class) -> WildexSearchTabRoute
        hasRoute(WildexSettingsTabRoute::class) -> WildexSettingsTabRoute
        else -> null
    }
}

fun NavOptionsBuilder.wildexMainBottomTabNavOptions() {
    popUpTo(WildexMainMenuRoute) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}

fun NavController.navigateToWildexMainBottomTab(tab: WildexMainBottomTabRoute) {
    when (tab) {
        WildexJournalTabRoute -> navigate(WildexJournalTabRoute) { wildexMainBottomTabNavOptions() }
        WildexCaptureTabRoute -> navigate(WildexCaptureTabRoute) { wildexMainBottomTabNavOptions() }
        WildexSearchTabRoute -> navigate(WildexSearchTabRoute) { wildexMainBottomTabNavOptions() }
        WildexSettingsTabRoute -> navigate(WildexSettingsTabRoute) { wildexMainBottomTabNavOptions() }
    }
}
