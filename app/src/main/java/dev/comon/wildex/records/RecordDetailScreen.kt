package dev.comon.wildex.records

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import dev.comon.wildex.component.WildexConfirmDialog
import dev.comon.wildex.component.rememberDebounceClick
import dev.comon.wildex.data.capture.CaptureRecordEntity
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecordDetailScreen(
    recordId: Long,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onDeleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: RecordDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RecordDetailViewModel(context.applicationContext as Application, recordId)
            }
        },
    )
    val record by viewModel.record.collectAsStateWithLifecycle()
    val memoInput by viewModel.memoInput.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        WildexConfirmDialog(
            titleText = "기록 삭제",
            messageText = "기록을 삭제하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            onDismiss = { showDeleteDialog = false },
            onConfirm = { viewModel.deleteRecord(onDeleted) },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            record == null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp),
                    color = WildexColorRoles.missionCtaBackground(),
                )
            }
            else -> RecordDetailContent(
                record = record!!,
                memo = memoInput ?: "",
                onMemoChange = viewModel::onMemoChange,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onDeleteClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecordDetailContent(
    record: CaptureRecordEntity,
    memo: String,
    onMemoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onDeleteClick: () -> Unit,
) {
    var showFullScreen by remember { mutableStateOf(false) }
    var retryKey by remember { mutableIntStateOf(0) }

    if (showFullScreen && record.imageUri.isNotBlank()) {
        RecordImageFullScreenViewer(
            imageUri = record.imageUri,
            contentDescription = record.name ?: "촬영 이미지",
            onDismiss = { showFullScreen = false },
            onImageLoaded = { retryKey++ },
        )
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        // 이미지
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
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(role = Role.Button) { showFullScreen = true },
            contentAlignment = Alignment.Center,
        ) {
            SubcomposeAsyncImage(
                model = record.imageUri,
                contentDescription = record.name ?: "촬영 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().then(sharedImageMod),
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                        color = WildexColorRoles.missionCtaBackground(),
                    )
                    is AsyncImagePainter.State.Error -> Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    else -> SubcomposeAsyncImageContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))

        // 정보 카드
        val depth = WildexDimens.shadowOffsetHard
        Box(
            modifier = Modifier
                .padding(horizontal = WildexDimens.gridMajor)
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
                    .padding(horizontal = WildexDimens.gridMajor, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                RecordInfoRow(label = "NAME", value = record.name ?: "미확인")
                RecordDivider()
                RecordInfoRow(label = "CATEGORY", value = record.category ?: "—")
                RecordDivider()
                RecordInfoRow(label = "DATE", value = formatDetailTimestamp(record.capturedAt))
                RecordDivider()
                RecordInfoRow(label = "LOCATION", value = record.address)
                if (record.latitude != null && record.longitude != null) {
                    RecordDivider()
                    RecordInfoRow(
                        label = "GPS",
                        value = "%.5f, %.5f".format(record.latitude, record.longitude),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))

        MemoCard(
            memo = memo,
            onMemoChange = onMemoChange,
            modifier = Modifier.padding(horizontal = WildexDimens.gridMajor),
        )

        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))

        // 삭제 버튼 (오른쪽 정렬)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WildexDimens.gridMajor),
            horizontalArrangement = Arrangement.End,
        ) {
            DeleteButton(onClick = onDeleteClick)
        }

        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val debouncedOnClick = rememberDebounceClick(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    var isVisuallyPressed by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isVisuallyPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel -> {
                    delay(150)
                    isVisuallyPressed = false
                }
            }
        }
    }
    val depth = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val shadowOffset = if (isVisuallyPressed) depthPressed else depth
    val contentInset = if (isVisuallyPressed) depth - depthPressed else 0.dp
    val deleteRed = MaterialTheme.colorScheme.error

    Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
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
                .background(deleteRed, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = debouncedOnClick,
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "삭제",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RecordInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = WildexColorRoles.missionCtaBackground(),
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RecordDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .height(WildexDimens.borderStrokeChunky)
            .background(WildexTheme.extraColors.cartridgeOutline),
        color = WildexTheme.extraColors.cartridgeOutline,
        thickness = WildexDimens.borderStrokeChunky,
    )
}

@Composable
private fun MemoCard(
    memo: String,
    onMemoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val depth = WildexDimens.shadowOffsetHard
    Box(modifier = modifier.padding(end = depth, bottom = depth)) {
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
                .padding(horizontal = WildexDimens.gridMajor, vertical = 12.dp),
        ) {
            Text(
                text = "MEMO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = WildexColorRoles.missionCtaBackground(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            BasicTextField(
                value = memo,
                onValueChange = onMemoChange,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
            ) { innerTextField ->
                Box {
                    if (memo.isEmpty()) {
                        Text(
                            text = "메모를 입력하세요",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "${memo.length} / $MEMO_MAX_LENGTH",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = if (memo.length >= MEMO_MAX_LENGTH) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val MEMO_MAX_LENGTH = 500

private fun formatDetailTimestamp(millis: Long): String =
    SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(Date(millis))

@Composable
private fun RecordImageFullScreenViewer(
    imageUri: String,
    contentDescription: String,
    onDismiss: () -> Unit,
    onImageLoaded: () -> Unit = {},
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val containerW = constraints.maxWidth.toFloat()
            val containerH = constraints.maxHeight.toFloat()
            fun maxOffsetX(s: Float) = (containerW * (s - 1f) / 2f).coerceAtLeast(0f)
            fun maxOffsetY(s: Float) = (containerH * (s - 1f) / 2f).coerceAtLeast(0f)

            SubcomposeAsyncImage(
                model = imageUri,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        coroutineScope {
                            launch {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale != 1f) {
                                            scale = 1f; offsetX = 0f; offsetY = 0f
                                        } else {
                                            scale = 2f
                                        }
                                    },
                                )
                            }
                            launch {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(0.5f, 2f)
                                    scale = newScale
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX(newScale), maxOffsetX(newScale))
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY(newScale), maxOffsetY(newScale))
                                }
                            }
                        }
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY),
                contentScale = ContentScale.Fit,
            ) {
                val painterState = painter.state
                LaunchedEffect(painterState) {
                    if (painterState is AsyncImagePainter.State.Success) onImageLoaded()
                }
                when (painterState) {
                    is AsyncImagePainter.State.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    }
                    is AsyncImagePainter.State.Error -> Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BrokenImage,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp),
                        )
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // 닫기 버튼 (우상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .border(WildexDimens.borderStrokeChunky, Color.White, RectangleShape)
                    .background(Color.Black.copy(alpha = 0.6f), RectangleShape)
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            // 줌 레벨 표시 (좌하단)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 48.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RectangleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "×${"%.1f".format(scale)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White,
                )
            }
        }
    }
}
