package dev.comon.wildex.records

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import dev.comon.wildex.component.WildexClickableCard
import dev.comon.wildex.component.WildexConfirmDialog
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.navigation.WildexRecordDetailRoute
import dev.comon.wildex.navigation.WildexRecordsListRoute
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation

private const val LONG_PRESS_DURATION_MS = 1000L

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecordsScreen(
    modifier: Modifier = Modifier,
    onBackNavigationState: (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { _, _ -> },
    onEditModeStateChanged: (
        isEditMode: Boolean,
        selectedCount: Int,
        onExitEditMode: () -> Unit,
        onRequestDelete: () -> Unit,
    ) -> Unit = { _, _, _, _ -> },
    onSearchModeStateChanged: (
        isSearchMode: Boolean,
        category: RecordsSearchCategory,
        pendingQuery: String,
        onCategoryChange: (RecordsSearchCategory) -> Unit,
        onQueryChange: (String) -> Unit,
        onSubmit: () -> Unit,
        onExitSearch: () -> Unit,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    pendingRecordId: Long? = null,
    onPendingRecordIdConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val canNavigateBack = backStackEntry != null && navController.previousBackStackEntry != null

    SideEffect {
        onBackNavigationState(canNavigateBack) { navController.popBackStack() }
    }

    val viewModel: RecordsViewModel = viewModel()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val isSearchMode by viewModel.isSearchMode.collectAsStateWithLifecycle()
    val searchCategory by viewModel.searchCategory.collectAsStateWithLifecycle()
    val pendingQuery by viewModel.pendingQuery.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isEditMode, selectedIds) {
        onEditModeStateChanged(
            isEditMode,
            selectedIds.size,
            viewModel::exitEditMode,
            { showDeleteDialog = true },
        )
    }

    LaunchedEffect(isSearchMode, searchCategory, pendingQuery) {
        onSearchModeStateChanged(
            isSearchMode,
            searchCategory,
            pendingQuery,
            viewModel::setSearchCategory,
            viewModel::onPendingQueryChange,
            viewModel::submitSearch,
            viewModel::exitSearchMode,
        )
    }

    LaunchedEffect(pendingRecordId) {
        if (pendingRecordId != null) {
            viewModel.exitEditMode()
            navController.popBackStack(WildexRecordsListRoute, inclusive = false)
            navController.navigate(WildexRecordDetailRoute(pendingRecordId))
            onPendingRecordIdConsumed()
        }
    }

    BackHandler(enabled = isEditMode && !canNavigateBack) {
        viewModel.exitEditMode()
    }

    BackHandler(enabled = isSearchMode && !canNavigateBack) {
        viewModel.exitSearchMode()
    }

    if (showDeleteDialog) {
        WildexConfirmDialog(
            titleText = "기록 삭제",
            messageText = "선택한 기록을 삭제하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            onDismiss = { showDeleteDialog = false },
            onConfirm = { viewModel.deleteSelected() },
        )
    }

    if (isDeleting) {
        RecordsDeletingDialog()
    }

    val dateFilterStartMillis by viewModel.dateFilterStartMillis.collectAsStateWithLifecycle()
    val dateFilterEndMillis by viewModel.dateFilterEndMillis.collectAsStateWithLifecycle()

    if (showDateDialog) {
        RecordsDateFilterDialog(
            initialStartMillis = dateFilterStartMillis,
            initialEndMillis = dateFilterEndMillis,
            onDismiss = { showDateDialog = false },
            onApply = { start, end -> viewModel.setDateFilter(start, end) },
            onClearAll = { viewModel.setDateFilter(null, null) },
        )
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = WildexRecordsListRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<WildexRecordsListRoute>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
            ) {
                RecordsListContent(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    viewModel = viewModel,
                    onRecordClick = { id -> navController.navigate(WildexRecordDetailRoute(id)) },
                    onCalendarClick = { showDateDialog = true },
                )
            }
            composable<WildexRecordDetailRoute>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) },
            ) { entry ->
                val route = entry.toRoute<WildexRecordDetailRoute>()
                RecordDetailScreen(
                    recordId = route.recordId,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onDeleted = { navController.popBackStack() },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecordsListContent(
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onRecordClick: (Long) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    viewModel: RecordsViewModel = viewModel(),
) {
    val lazyItems = viewModel.records.collectAsLazyPagingItems()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
    val dateFilterStartMillis by viewModel.dateFilterStartMillis.collectAsStateWithLifecycle()
    val dateFilterEndMillis by viewModel.dateFilterEndMillis.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyItems.loadState) {
        if(lazyItems.loadState.refresh is LoadState.NotLoading && lazyItems.itemCount != 0)
            lazyListState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RecordsTopToolbar(
            isDateFilterActive = dateFilterStartMillis != null || dateFilterEndMillis != null,
            isSortAscending = sortAscending,
            onCalendarClick = onCalendarClick,
            onSortToggle = viewModel::toggleSort,
            onSearchClick = viewModel::enterSearchMode,
        )
        Box(modifier = Modifier.weight(1f)) {
        when {
            lazyItems.loadState.refresh is LoadState.Loading && lazyItems.itemCount == 0 -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WildexColorRoles.missionCtaBackground(),
                )
            }
            lazyItems.loadState.refresh is LoadState.Error && lazyItems.itemCount == 0 -> {
                val msg = (lazyItems.loadState.refresh as LoadState.Error).error.message
                    ?: "기록을 불러올 수 없습니다"
                RecordsErrorState(
                    message = msg,
                    onRetry = { lazyItems.retry() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            lazyItems.itemCount == 0 && lazyItems.loadState.refresh is LoadState.NotLoading -> {
                RecordsEmptyState(modifier = Modifier.fillMaxSize())
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        horizontal = WildexDimens.gridMajor,
                        vertical = WildexDimens.gridMajor,
                    ),
                    verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
                ) {
                    items(
                        count = lazyItems.itemCount,
                        key = { index -> lazyItems.peek(index)?.id ?: index },
                    ) { index ->
                        val record = lazyItems[index]
                        if (record != null) {
                            RecordsCard(
                                record = record,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                isEditMode = isEditMode,
                                isSelected = selectedIds.contains(record.id),
                                onEnterEditMode = { viewModel.enterEditMode(record.id) },
                                onToggleSelect = { viewModel.toggleSelected(record.id) },
                                onOpen = { onRecordClick(record.id) },
                            )
                        }
                    }
                    when (lazyItems.loadState.append) {
                        is LoadState.Loading -> item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = WildexColorRoles.missionCtaBackground(),
                                )
                            }
                        }
                        is LoadState.Error -> item {
                            val errMsg = (lazyItems.loadState.append as LoadState.Error).error.message
                                ?: "더 불러올 수 없습니다"
                            RecordsAppendError(
                                message = errMsg,
                                onRetry = { lazyItems.retry() },
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecordsCard(
    record: CaptureRecordEntity,
    modifier: Modifier = Modifier,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onEnterEditMode: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
    onOpen: () -> Unit = {},
) {
    val longPressMod = if (!isEditMode) {
        Modifier.pointerInput(record.id) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                try {
                    withTimeout(LONG_PRESS_DURATION_MS) {
                        waitForUpOrCancellation()
                    }
                } catch (_: PointerEventTimeoutCancellationException) {
                    onEnterEditMode()
                }
            }
        }
    } else Modifier

    Box(modifier = modifier.then(longPressMod)) {
        WildexClickableCard(
            onClick = if (isEditMode) onToggleSelect else onOpen,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 썸네일
            val sharedImageMod: Modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "record_image_${record.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            } else Modifier
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = record.imageUri,
                    contentDescription = record.name ?: "미확인",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().then(sharedImageMod),
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = WildexColorRoles.missionCtaBackground(),
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
            // 텍스트
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = record.name ?: "미확인",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = formatTimestamp(record.capturedAt),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            // 편집모드 체크박스
            if (isEditMode) {
                RecordsSelectionCheckbox(checked = isSelected)
            }
        }
    }
}

@Composable
private fun RecordsSelectionCheckbox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
            .background(
                if (checked) WildexColorRoles.missionCtaBackground()
                else MaterialTheme.colorScheme.surfaceContainerLowest,
                RectangleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = WildexColorRoles.missionCtaForeground(),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordsDeletingDialog() {
    val depth = WildexDimens.shadowOffsetHard
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(end = depth, bottom = depth),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(depth, depth)
                    .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape)
                    .padding(horizontal = WildexDimens.gridMajor, vertical = WildexDimens.gridMajor),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
            ) {
                Text(
                    text = "삭제하는 중",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp,
                    color = WildexColorRoles.missionCtaBackground(),
                )
            }
        }
    }
}

@Composable
private fun RecordsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(WildexDimens.gridMajor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "기록이 없습니다",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "카메라로 촬영하면 여기 기록됩니다",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecordsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val debouncedOnRetry = dev.comon.wildex.component.rememberDebounceClick(onRetry)
    val depth = WildexDimens.shadowOffsetHard
    Column(
        modifier = modifier.padding(WildexDimens.gridMajor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "ERROR",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = WildexColorRoles.missionCtaBackground(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))
        Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(depth, depth)
                    .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
            )
            Box(
                modifier = Modifier
                    .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                    .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                    .clickable(role = Role.Button, onClick = debouncedOnRetry)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "RETRY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = WildexColorRoles.missionCtaForeground(),
                )
            }
        }
    }
}

@Composable
private fun RecordsAppendError(
    message: String,
    onRetry: () -> Unit,
) {
    val debouncedOnRetry = dev.comon.wildex.component.rememberDebounceClick(onRetry)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                .clickable(role = Role.Button, onClick = debouncedOnRetry)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                text = "RETRY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = WildexColorRoles.missionCtaForeground(),
            )
        }
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RecordsCardPreview() {
    WildexTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecordsCard(
                record = CaptureRecordEntity(
                    id = 1,
                    imageUri = "",
                    capturedAt = System.currentTimeMillis(),
                    latitude = 37.5665,
                    longitude = 126.9780,
                    address = "서울특별시 중구",
                    name = "황조롱이",
                    category = "맹금류",
                ),
            )
            RecordsCard(
                record = CaptureRecordEntity(
                    id = 2,
                    imageUri = "",
                    capturedAt = System.currentTimeMillis() - 3600_000,
                    latitude = null,
                    longitude = null,
                    address = "권한 거절",
                    name = null,
                    category = null,
                ),
                isEditMode = true,
                isSelected = true,
            )
        }
    }
}
