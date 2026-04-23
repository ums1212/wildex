package dev.comon.wildex.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.comon.wildex.R
import dev.comon.wildex.component.WildexLogoutConfirmDialog
import dev.comon.wildex.component.WildexMenuButton
import dev.comon.wildex.component.WildexMenuButtonStyle
import dev.comon.wildex.navigation.WildexCaptureTabRoute
import dev.comon.wildex.navigation.WildexJournalTabRoute
import dev.comon.wildex.navigation.WildexMainBottomTabRoute
import dev.comon.wildex.navigation.WildexMainMenuRoute
import dev.comon.wildex.navigation.WildexRecordsTabRoute
import dev.comon.wildex.navigation.WildexSettingsTabRoute
import dev.comon.wildex.navigation.navigateToWildexMainBottomTab
import dev.comon.wildex.navigation.wildexSelectedMainBottomTab
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.navigation.toRoute
import dev.comon.wildex.capture.CaptureResultScreen
import dev.comon.wildex.capture.CaptureScreen
import dev.comon.wildex.journal.JournalScreen
import dev.comon.wildex.navigation.WildexCaptureResultRoute
import dev.comon.wildex.records.RecordsScreen
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.util.Locale

private data class MainMenuBottomTabUi(
    val route: WildexMainBottomTabRoute,
    val icon: ImageVector,
    val label: String,
)

private val mainMenuBottomTabUiRows: List<MainMenuBottomTabUi> = listOf(
    MainMenuBottomTabUi(WildexJournalTabRoute, Icons.AutoMirrored.Filled.MenuBook, "JOURNAL"),
    MainMenuBottomTabUi(WildexCaptureTabRoute, Icons.Filled.CameraAlt, "CAPTURE"),
    MainMenuBottomTabUi(WildexRecordsTabRoute, Icons.Filled.PhotoLibrary, "RECORDS"),
    MainMenuBottomTabUi(WildexSettingsTabRoute, Icons.Filled.Settings, "SETTINGS"),
)

/** 테두리·하드 섀도 예약·아이콘·라벨이 잘리지 않도록 하는 하단 탭 행 높이 */
private val MainMenuBottomBarHeight = 72.dp

private fun WildexMainBottomTabRoute.mainMenuTabLabel(): String =
    mainMenuBottomTabUiRows.first { it.route == this }.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainMenuScreen(
    isLoggedIn: Boolean,
    userNickname: String,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    /** 미리보기·테스트에서 다이얼로그를 처음부터 열 때만 true. 앱에서는 기본 false. */
    initialLogoutDialogOpen: Boolean = false,
) {
    var showLogoutDialog by remember(initialLogoutDialogOpen) {
        mutableStateOf(initialLogoutDialogOpen)
    }
    var showExitDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedBottomTab = navBackStackEntry?.destination.wildexSelectedMainBottomTab()
    val currentDestination = navBackStackEntry?.destination
    val isAtHome = currentDestination?.hasRoute<WildexMainMenuRoute>() == true
    BackHandler(enabled = isAtHome) { showExitDialog = true }

    if (showExitDialog) {
        WildexLogoutConfirmDialog(
            titleText = "종료",
            messageText = "앱을 종료하시겠습니까?",
            confirmText = "종료",
            dismissText = "취소",
            onDismiss = { showExitDialog = false },
            onConfirmLogout = { activity?.finish() },
        )
    }

    // Journal / Records 탭 내부 NavHost의 뒤로가기 상태
    var journalCanNavigateBack by remember { mutableStateOf(false) }
    var journalOnBack: () -> Unit by remember { mutableStateOf({}) }
    var recordsCanNavigateBack by remember { mutableStateOf(false) }
    var recordsOnBack: () -> Unit by remember { mutableStateOf({}) }

    // 스크롤 기반 bars 슬라이드 (BirdList/BirdInfo 화면에서만 활성)
    var barsVisible by remember { mutableStateOf(true) }
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    // 캡처 분석 중에는 하단 바 강제 숨김
    var captureAnalyzing by remember { mutableStateOf(false) }
    // 캡처 화면에서 인식된 조류 speciesId — Journal로 이동 후 소비됨
    var pendingCaptureSpeciesId by remember { mutableStateOf<String?>(null) }
    // 캡처 저장된 recordId — BirdInfoScreen 스낵바 표시 후 소비됨
    var pendingCaptureRecordId by remember { mutableStateOf<Long?>(null) }
    // BirdInfoScreen "이동" 액션 후 Records 탭으로 점프할 recordId
    var pendingRecordDetailId by remember { mutableStateOf<Long?>(null) }
    val topBarTranslation by animateFloatAsState(
        targetValue = if (barsVisible && !captureAnalyzing) 0f else -topBarHeightPx.toFloat(),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
    val bottomBarTranslation by animateFloatAsState(
        targetValue = if (barsVisible && !captureAnalyzing) 0f else bottomBarHeightPx.toFloat(),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
    val bottomBarEntryProgress by animateFloatAsState(
        targetValue = if (selectedBottomTab != null) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "bottomBarEntry",
    )

    LaunchedEffect(journalCanNavigateBack) {
        if (!journalCanNavigateBack) barsVisible = true
    }

    LaunchedEffect(recordsCanNavigateBack) {
        if (!recordsCanNavigateBack) barsVisible = true
    }

    LaunchedEffect(selectedBottomTab) {
        if (selectedBottomTab != WildexCaptureTabRoute) captureAnalyzing = false
        if (selectedBottomTab != WildexRecordsTabRoute) barsVisible = true
    }

    val selectedBottomTabRef = rememberUpdatedState(selectedBottomTab)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (journalCanNavigateBack || selectedBottomTabRef.value == WildexRecordsTabRoute) {
                    if (available.y < -3f) barsVisible = false
                    else if (available.y > 3f) barsVisible = true
                }
                return Offset.Zero
            }
        }
    }

    if (showLogoutDialog) {
        WildexLogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = onLogout,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            val topBarAnimModifier = animatedVisibilityScope?.run {
                Modifier.animateEnterExit(
                    enter = slideInVertically(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        initialOffsetY = { -it },
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        targetOffsetY = { -it },
                    ),
                )
            } ?: Modifier
            Column(
                modifier = topBarAnimModifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .onGloballyPositioned { topBarHeightPx = it.size.height }
                    .graphicsLayer { translationY = topBarTranslation },
            ) {
                val showBackButton =
                    (selectedBottomTab == WildexJournalTabRoute && journalCanNavigateBack) ||
                    (selectedBottomTab == WildexRecordsTabRoute && recordsCanNavigateBack)
                val backOnClick: () -> Unit = when {
                    selectedBottomTab == WildexRecordsTabRoute && recordsCanNavigateBack -> recordsOnBack
                    else -> journalOnBack
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (showBackButton) {
                        MainMenuTopBarBackButton(onClick = backOnClick)
                    } else {
                        MainMenuTopBarDpadIcon()
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoggedIn) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .border(
                                        width = WildexDimens.borderStrokeChunky,
                                        color = WildexTheme.extraColors.cartridgeOutline,
                                        shape = RectangleShape,
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        shape = RectangleShape,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = userNickname.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            Text(
                                text = "로그인",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable(
                                    onClick = onLoginClick,
                                ),
                            )
                        }
                    }
                    if (isLoggedIn) {
                        MainMenuProfileAvatar(
                            onClick = { showLogoutDialog = true },
                        )
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WildexDimens.borderStrokeChunky)
                        .background(WildexTheme.extraColors.cartridgeOutline),
                )
            }
        },
        bottomBar = {
            val bottomBarAnimModifier = animatedVisibilityScope?.run {
                Modifier.animateEnterExit(
                    enter = slideInVertically(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        initialOffsetY = { it },
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        targetOffsetY = { it },
                    ),
                )
            } ?: Modifier
            AnimatedVisibility(
                visible = selectedBottomTab != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ),
            ) {
                Box(
                    modifier = bottomBarAnimModifier
                        .onGloballyPositioned { bottomBarHeightPx = it.size.height }
                        .graphicsLayer { translationY = bottomBarTranslation },
                ) {
                    MainMenuBottomBar(
                        selectedTab = selectedBottomTab,
                        onTabClick = { tab ->
                            val destination = navBackStackEntry?.destination
                            if (destination.wildexSelectedMainBottomTab() == tab) {
                                navController.popBackStack(WildexMainMenuRoute, inclusive = false)
                            } else {
                                navController.navigateToWildexMainBottomTab(tab)
                            }
                        },
                    )
                }
            }
        },
    ) { _ ->
        val contentAnimModifier = animatedVisibilityScope?.run {
            Modifier.animateEnterExit(
                enter = fadeIn(tween(1000, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(1000, easing = FastOutSlowInEasing)),
            )
        } ?: Modifier
        // innerPadding 대신 측정된 px 높이 + 현재 translation으로 직접 계산
        // → bars 이동과 content padding이 같은 값을 공유해 완전히 동기화
        val density = LocalDensity.current
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarPx = WindowInsets.navigationBars.getBottom(density).toFloat()
        val contentTopPadding = with(density) {
            (topBarHeightPx.toFloat() + topBarTranslation).coerceAtLeast(0f).toDp()
        } + statusBarPadding
        val contentBottomPadding = with(density) {
            val barHeight = bottomBarHeightPx.toFloat() * bottomBarEntryProgress - bottomBarTranslation
            barHeight.coerceAtLeast(navBarPx).toDp()
        }

        SharedTransitionLayout(
            modifier = contentAnimModifier
                .padding(top = contentTopPadding, bottom = contentBottomPadding)
                .fillMaxSize(),
        ) {
        NavHost(
            navController = navController,
            startDestination = WildexMainMenuRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<WildexMainMenuRoute>(
                enterTransition = {
                    scaleIn(
                        initialScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    scaleIn(
                        initialScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    scaleOut(
                        targetScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
            ) {
                MainMenuHomeContent(
                    onCaptureClick = {
                        navController.navigateToWildexMainBottomTab(WildexCaptureTabRoute)
                    },
                    onJournalClick = {
                        navController.navigateToWildexMainBottomTab(WildexJournalTabRoute)
                    },
                    onSettingsClick = {
                        navController.navigateToWildexMainBottomTab(WildexSettingsTabRoute)
                    },
                    onRecordsClick = {
                        navController.navigateToWildexMainBottomTab(WildexRecordsTabRoute)
                    },
                )
            }
            composable<WildexJournalTabRoute>(
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
            ) {
                JournalScreen(
                    onBackNavigationState = { canNavigateBack, onBack, _ ->
                        journalCanNavigateBack = canNavigateBack
                        journalOnBack = onBack
                    },
                    pendingSpeciesId = pendingCaptureSpeciesId,
                    pendingRecordId = pendingCaptureRecordId,
                    onPendingSpeciesIdConsumed = {
                        pendingCaptureSpeciesId = null
                        pendingCaptureRecordId = null
                    },
                    onNavigateToRecordDetail = { recordId ->
                        pendingRecordDetailId = recordId
                        navController.navigateToWildexMainBottomTab(WildexRecordsTabRoute)
                    },
                )
            }
            composable<WildexCaptureTabRoute>(
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popEnterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
            ) {
                CaptureScreen(
                    onNavigateToBirdInfo = { speciesId, recordId ->
                        pendingCaptureSpeciesId = speciesId
                        pendingCaptureRecordId = recordId
                        navController.navigateToWildexMainBottomTab(WildexJournalTabRoute)
                    },
                    onAnalyzingChanged = { captureAnalyzing = it },
                )
            }
            composable<WildexCaptureResultRoute>(
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<WildexCaptureResultRoute>()
                CaptureResultScreen(speciesId = route.speciesId)
            }
            composable<WildexRecordsTabRoute>(
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
            ) {
                RecordsScreen(
                    onBackNavigationState = { canNavigateBack, onBack ->
                        recordsCanNavigateBack = canNavigateBack
                        recordsOnBack = onBack
                    },
                    pendingRecordId = pendingRecordDetailId,
                    onPendingRecordIdConsumed = { pendingRecordDetailId = null },
                )
            }
            composable<WildexSettingsTabRoute>(
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    )
                },
            ) {
                SettingsScreen()
            }
        }
        } // SharedTransitionLayout
    }
}

@Composable
private fun MainMenuHomeContent(
    onCaptureClick: () -> Unit,
    onJournalClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecordsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MainMenuSectionLabel()
        MainMenuScreenMenuButton(
            titleText = "Capture",
            subtitleText = "Scan new specimen",
            imageVector = Icons.Filled.CameraAlt,
            onClick = onCaptureClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .fillMaxHeight(),
            style = WildexMenuButtonStyle.Primary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MainMenuScreenMenuButton(
                titleText = "Journal",
                subtitleText = "Daily record",
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onJournalClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                style = WildexMenuButtonStyle.Secondary,
            )
            MainMenuScreenMenuButton(
                titleText = "Settings",
                subtitleText = "Configure",
                imageVector = Icons.Filled.Settings,
                onClick = onSettingsClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                style = WildexMenuButtonStyle.Secondary,
            )
        }
        MainMenuScreenMenuButton(
            titleText = "Records",
            subtitleText = "Capture history",
            imageVector = Icons.Filled.PhotoLibrary,
            onClick = onRecordsClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .fillMaxHeight(),
            style = WildexMenuButtonStyle.Secondary,
        )
    }
}

/** 메인 메뉴 전용 [WildexMenuButton] 래퍼: 작은 단말에서도 한 화면에 모두 보이도록 세로 패딩 0. */
@Composable
private fun MainMenuScreenMenuButton(
    titleText: String,
    subtitleText: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WildexMenuButtonStyle? = null,
) {
    WildexMenuButton(
        titleText = titleText,
        subtitleText = subtitleText,
        imageVector = imageVector,
        onClick = onClick,
        modifier = modifier,
        style = style,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    )
}

@Composable
private fun MainMenuTopBarDpadIcon() {
    Image(
        painter = painterResource(R.drawable.dpad),
        contentDescription = "컨트롤러",
        modifier = Modifier.size(32.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun MainMenuTopBarBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depth = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val shadowOffset = if (pressed) depthPressed else depth
    val contentInset = if (pressed) depth - depthPressed else 0.dp

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
        )
        Box(
            modifier = Modifier
                .offset(contentInset, contentInset)
                .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MainMenuProfileAvatar(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(
                width = WildexDimens.borderStrokeChunky,
                color = WildexTheme.extraColors.cartridgeOutline,
                shape = CircleShape,
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "프로필",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun MainMenuSectionLabel() {
    val depth = WildexDimens.shadowOffsetHard
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
        )
        Box(
            modifier = Modifier
                .border(
                    width = WildexDimens.borderStrokeChunky,
                    color = WildexTheme.extraColors.cartridgeOutline,
                    shape = RectangleShape,
                )
                .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = "MAIN MENU",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = WildexColorRoles.missionCtaForeground(),
            )
        }
    }
}

@Composable
private fun MainMenuBottomBar(
    selectedTab: WildexMainBottomTabRoute?,
    onTabClick: (WildexMainBottomTabRoute) -> Unit,
) {
    val tabs = mainMenuBottomTabUiRows
    // navigationBarsPadding을 Row와 같은 높이 제한에 두면, 패딩이 Row 안쪽에서 높이를 잡아먹어 탭이 세로로 잘림.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MainMenuBottomBarHeight)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    MainMenuBottomBarTabCell(
                        selected = selected,
                        tab = tab,
                        onClick = { onTabClick(tab.route) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuBottomBarTabCell(
    selected: Boolean,
    tab: MainMenuBottomTabUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depthNormal = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    /** 선택 탭은 항상 눌린(인셋) 상태, 비선택은 손가락 누를 때만 */
    val visuallyPressed = selected || pressed
    val shadowOffset = if (visuallyPressed) depthPressed else depthNormal
    val contentInset = if (visuallyPressed) depthNormal - depthPressed else 0.dp
    val faceColor =
        if (selected) WildexColorRoles.missionCtaBackground() else MaterialTheme.colorScheme.surfaceContainerLowest
    val contentColor =
        if (selected) WildexColorRoles.missionCtaForeground() else MaterialTheme.colorScheme.onSurface
    val frameColor = WildexTheme.extraColors.cartridgeOutline
    val shadowColor = WildexTheme.extraColors.cartridgeHardShadow

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = depthNormal, bottom = depthNormal),
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(shadowOffset, shadowOffset)
                        .background(shadowColor, RectangleShape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(contentInset, contentInset)
                        .clip(RectangleShape)
                        .border(WildexDimens.borderStrokeChunky, frameColor, RectangleShape)
                        .background(faceColor, RectangleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainMenuScreenPreviewLoggedIn() {
    WildexTheme {
        MainMenuScreen(
            isLoggedIn = true,
            userNickname = "TRAINER_L_10",
            onLoginClick = { },
            onLogout = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainMenuScreenPreviewLoggedOut() {
    WildexTheme {
        MainMenuScreen(
            isLoggedIn = false,
            userNickname = "",
            onLoginClick = { },
            onLogout = { },
        )
    }
}

@Preview(
    name = "로그아웃 다이얼로그",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun MainMenuScreenPreviewLogoutDialog() {
    WildexTheme {
        MainMenuScreen(
            isLoggedIn = true,
            userNickname = "TRAINER_L_10",
            onLoginClick = { },
            onLogout = { },
            initialLogoutDialogOpen = true,
        )
    }
}
