package dev.comon.wildex.records

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import androidx.compose.foundation.clickable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecordsScreen(
    modifier: Modifier = Modifier,
    onBackNavigationState: (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { _, _ -> },
    pendingRecordId: Long? = null,
    onPendingRecordIdConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val canNavigateBack = backStackEntry != null && navController.previousBackStackEntry != null

    SideEffect {
        onBackNavigationState(canNavigateBack) { navController.popBackStack() }
    }

    LaunchedEffect(pendingRecordId) {
        if (pendingRecordId != null) {
            navController.popBackStack(WildexRecordsListRoute, inclusive = false)
            navController.navigate(WildexRecordDetailRoute(pendingRecordId))
            onPendingRecordIdConsumed()
        }
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
                    onRecordClick = { id -> navController.navigate(WildexRecordDetailRoute(id)) },
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
    viewModel: RecordsViewModel = viewModel(),
) {
    val lazyItems = viewModel.records.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
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
                                onClick = { onRecordClick(record.id) },
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecordsCard(
    record: CaptureRecordEntity,
    modifier: Modifier = Modifier,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depth = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val shadowOffset = if (pressed) depthPressed else depth
    val contentInset = if (pressed) depth - depthPressed else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(contentInset, contentInset)
                .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    .clickable(role = Role.Button, onClick = onRetry)
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
                .clickable(role = Role.Button, onClick = onRetry)
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
            )
        }
    }
}
