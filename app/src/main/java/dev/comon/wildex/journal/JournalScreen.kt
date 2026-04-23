package dev.comon.wildex.journal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.comon.wildex.component.WildexMenuButton
import dev.comon.wildex.component.WildexMenuButtonStyle
import dev.comon.wildex.journal.birdinfo.BirdInfoScreen
import dev.comon.wildex.journal.birdlist.BirdListScreen
import dev.comon.wildex.navigation.WildexBirdInfoRoute
import dev.comon.wildex.navigation.WildexBirdListRoute
import dev.comon.wildex.navigation.WildexJournalCategoryRoute
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme

@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    onBackNavigationState: (canNavigateBack: Boolean, onBack: () -> Unit, title: String?) -> Unit = { _, _, _ -> },
    pendingSpeciesId: String? = null,
    pendingRecordId: Long? = null,
    onPendingSpeciesIdConsumed: () -> Unit = {},
    onNavigateToRecordDetail: (Long) -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val canNavigateBack = navController.previousBackStackEntry != null
    val screenTitle: String? = when {
        destination?.hasRoute<WildexBirdListRoute>() == true -> "AVIAN INDEX"
        destination?.hasRoute<WildexBirdInfoRoute>() == true -> "SPECIES INFO"
        else -> null
    }

    SideEffect {
        onBackNavigationState(canNavigateBack, { navController.popBackStack() }, screenTitle)
    }

    LaunchedEffect(pendingSpeciesId) {
        if (pendingSpeciesId != null) {
            navController.popBackStack(WildexJournalCategoryRoute, inclusive = false)
            navController.navigate(WildexBirdListRoute)
            navController.navigate(WildexBirdInfoRoute(pendingSpeciesId, pendingRecordId))
            onPendingSpeciesIdConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = WildexJournalCategoryRoute,
        modifier = modifier.fillMaxSize(),
    ) {
        composable<WildexJournalCategoryRoute>(
            enterTransition = { fadeIn(tween(300, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(300, easing = FastOutSlowInEasing)) },
            popEnterTransition = { fadeIn(tween(300, easing = FastOutSlowInEasing)) },
            popExitTransition = { fadeOut(tween(300, easing = FastOutSlowInEasing)) },
        ) {
            JournalCategoryScreen(
                onBirdListClick = {
                    navController.navigate(WildexBirdListRoute)
                },
            )
        }
        composable<WildexBirdListRoute>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
        ) {
            BirdListScreen(
                onBirdClick = { speciesId ->
                    navController.navigate(WildexBirdInfoRoute(speciesId))
                },
            )
        }
        composable<WildexBirdInfoRoute>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            },
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<WildexBirdInfoRoute>()
            BirdInfoScreen(
                speciesId = route.speciesId,
                savedRecordId = route.recordId,
                onNavigateToRecordDetail = onNavigateToRecordDetail,
            )
        }
    }
}

private data class JournalCategoryItem(
    val titleText: String,
    val subtitleText: String,
    val icon: ImageVector,
    val enabled: Boolean,
)

private val journalCategories = listOf(
    JournalCategoryItem("Birds", "Aves Classis", Icons.Filled.SetMeal, enabled = true),
    JournalCategoryItem("Mammals", "Mammalia Classis", Icons.Filled.Pets, enabled = false),
    JournalCategoryItem("Insects", "Insecta Classis", Icons.Filled.BugReport, enabled = false),
    JournalCategoryItem("Plants", "Plantae Kingdom", Icons.Filled.LocalFlorist, enabled = false),
)

@Composable
private fun JournalCategoryScreen(
    onBirdListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(WildexDimens.gridMajor),
        verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
    ) {
        journalCategories.forEach { category ->
            JournalScreenMenuButton(
                item = category,
                onClick = if (category.enabled && category.titleText == "Birds") onBirdListClick else ({}),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Journal 카테고리 화면 전용 [WildexMenuButton] 래퍼: 비활성 시 "준비중" 오버레이 + 기본 카트리지 패딩. */
@Composable
private fun JournalScreenMenuButton(
    item: JournalCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        WildexMenuButton(
            titleText = item.titleText,
            subtitleText = item.subtitleText,
            imageVector = item.icon,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            style = WildexMenuButtonStyle.Secondary,
            enabled = item.enabled,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        )
        // 비활성화 항목에 "준비중" 오버레이
        if (!item.enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "준비중",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = WildexColorRoles.missionCtaForeground(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun JournalCategoryScreenPreview() {
    WildexTheme {
        JournalCategoryScreen(onBirdListClick = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JournalCategoryScreenDarkPreview() {
    WildexTheme(darkTheme = true) {
        JournalCategoryScreen(onBirdListClick = {})
    }
}
