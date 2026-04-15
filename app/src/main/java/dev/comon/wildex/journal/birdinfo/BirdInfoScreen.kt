package dev.comon.wildex.journal.birdinfo

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import dev.comon.wildex.audio.LocalBgmManager
import dev.comon.wildex.component.WildexCircleCartridgePressButton
import dev.comon.wildex.domain.model.BirdDetail
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme

@Composable
fun BirdInfoScreen(
    speciesId: String,
    modifier: Modifier = Modifier,
    viewModel: BirdInfoViewModel = viewModel(),
) {
    val context = LocalContext.current
    val bgmManager = LocalBgmManager.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(BirdInfoIntent.StopTts)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(speciesId) {
        viewModel.onIntent(BirdInfoIntent.Load(speciesId))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BirdInfoUiEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is BirdInfoUiEvent.ShowTtsError ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                BirdInfoUiEvent.DuckBgm -> bgmManager.setVolume(0.2f)
                BirdInfoUiEvent.RestoreBgm -> bgmManager.setVolume(1f)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WildexColorRoles.missionCtaBackground())
                    }
                }
                state.error != null && state.bird == null -> {
                    BirdInfoErrorState(
                        message = state.error!!,
                        onRetry = { viewModel.onIntent(BirdInfoIntent.Retry(speciesId)) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.bird != null -> {
                    BirdInfoContent(
                        bird = state.bird!!,
                        isSpeaking = state.isSpeaking,
                        onTtsClick = { viewModel.onIntent(BirdInfoIntent.ToggleTts) },
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }
    }
}

@Composable
private fun BirdInfoContent(
    bird: BirdDetail,
    isSpeaking: Boolean,
    onTtsClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var showFullScreen by remember { mutableStateOf(false) }
    var retryKey by remember { mutableIntStateOf(0) }

    if (showFullScreen && bird.imageUrl.isNotBlank()) {
        BirdImageFullScreenViewer(
            imageUrl = bird.imageUrl,
            contentDescription = bird.name,
            onDismiss = { showFullScreen = false },
            onImageLoaded = { retryKey++ },
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        // 상단 이미지 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        ) {
            if (bird.imageUrl.isNotBlank()) {
                key(retryKey) {
                    SubcomposeAsyncImage(
                        model = bird.imageUrl,
                        contentDescription = bird.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(role = Role.Button) { showFullScreen = true },
                        contentScale = ContentScale.Crop,
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp,
                                        color = WildexColorRoles.missionCtaBackground(),
                                    )
                                }
                            }
                            is AsyncImagePainter.State.Error -> {
                                val errorState = painter.state as AsyncImagePainter.State.Error
                                Log.e(
                                    "BirdInfoImage",
                                    "이미지 로드 실패 url=${bird.imageUrl}",
                                    errorState.result.throwable,
                                )
                                BirdImagePlaceholder(
                                    label = "IMAGE ERROR",
                                    subtitle = "click and retry",
                                )
                            }
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                }
            } else {
                BirdImagePlaceholder(label = "NO IMAGE")
            }
            // 종 번호 배지 (이미지 우하단)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(WildexDimens.gridMajor)
                    .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "AVE-${bird.speciesId.takeLast(4).padStart(4, '0')}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = WildexColorRoles.missionCtaForeground(),
                )
            }
        }

        // 두꺼운 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WildexDimens.borderStrokeChunky)
                .background(WildexTheme.extraColors.cartridgeOutline),
        )

        // 본문 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(WildexDimens.gridMajor),
            verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
        ) {
            // 이름 섹션
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = bird.name.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = bird.scientificName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WildexDimens.borderStrokeChunky)
                    .background(WildexTheme.extraColors.cartridgeOutline),
            )

            // TTS 스피커 버튼 (generalFeature가 있을 때만 표시)
            if (bird.generalFeature.isNotBlank()) {
                TtsSpeakerButton(
                    isSpeaking = isSpeaking,
                    onClick = onTtsClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // 일반 특징
            if (bird.generalFeature.isNotBlank()) {
                Text(
                    text = bird.generalFeature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // 분류 정보 카트리지 블록
            BirdTaxonomyBlock(bird = bird)

            // 생태 특징
            if (bird.ecologicalFeature.isNotBlank()) {
                BirdInfoDataRow(label = "생태", value = bird.ecologicalFeature)
            }

            Spacer(modifier = Modifier.height(WildexDimens.gridMajor))
        }
    }
}

@Composable
private fun TtsSpeakerButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctaColor = WildexColorRoles.missionCtaBackground()

    val infiniteTransition = rememberInfiniteTransition(label = "tts_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    WildexCircleCartridgePressButton(
        onClick = onClick,
        modifier = modifier,
        isActive = isSpeaking,
        backgroundColor = ctaColor,
        buttonModifier = Modifier.drawBehind {
            if (isSpeaking) {
                drawCircle(
                    color = ctaColor.copy(alpha = glowAlpha),
                    radius = size.minDimension / 2f + 10.dp.toPx(),
                )
            }
        },
    ) {
        Icon(
            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = if (isSpeaking) "TTS 정지" else "TTS 재생",
            tint = WildexColorRoles.missionCtaForeground(),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun BirdImageFullScreenViewer(
    imageUrl: String,
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

            // 이미지 경계 계산:
            // graphicsLayer 스케일은 composable 중심 기준으로 확대하므로
            // 이동 가능한 최대 범위 = 컨테이너 크기 × (scale - 1) / 2
            fun maxOffsetX(s: Float) = (containerW * (s - 1f) / 2f).coerceAtLeast(0f)
            fun maxOffsetY(s: Float) = (containerH * (s - 1f) / 2f).coerceAtLeast(0f)

            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        coroutineScope {
                            // 더블탭: 1x ↔ 2x 토글
                            launch {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale != 1f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2f
                                        }
                                    },
                                )
                            }
                            // 핀치 줌 + 드래그 (경계 제한)
                            launch {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(0.5f, 2f)
                                    scale = newScale
                                    // 스케일 변경 후 경계 재계산
                                    val mX = maxOffsetX(newScale)
                                    val mY = maxOffsetY(newScale)
                                    offsetX = (offsetX + pan.x).coerceIn(-mX, mX)
                                    offsetY = (offsetY + pan.y).coerceIn(-mY, mY)
                                }
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                    ),
                contentScale = ContentScale.Fit,
            ) {
                val painterState = painter.state
                LaunchedEffect(painterState) {
                    if (painterState is AsyncImagePainter.State.Success) {
                        onImageLoaded()
                    }
                }
                when (painterState) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        }
                    }
                    is AsyncImagePainter.State.Error -> BirdImagePlaceholder(label = "IMAGE ERROR")
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

@Composable
private fun BirdImagePlaceholder(label: String, subtitle: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun BirdTaxonomyBlock(bird: BirdDetail) {
    val depth = WildexDimens.shadowOffsetHard
    Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
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
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape),
        ) {
            // 블록 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WildexColorRoles.missionCtaBackground())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "TAXONOMIC CLASSIFICATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = WildexColorRoles.missionCtaForeground(),
                )
            }
            val rows = listOf(
                "PHYLUM" to if (bird.phylumName.isNotBlank()) "${bird.phylumName} (${bird.phylumEngName})" else bird.phylumEngName,
                "CLASS" to if (bird.className.isNotBlank()) "${bird.className} (${bird.classEngName})" else bird.classEngName,
                "ORDER" to if (bird.orderName.isNotBlank()) "${bird.orderName} (${bird.orderEngName})" else bird.orderEngName,
                "FAMILY" to if (bird.familyName.isNotBlank()) "${bird.familyName} (${bird.familyEngName})" else bird.familyEngName,
                "GENUS" to if (bird.genusName.isNotBlank()) "${bird.genusName} (${bird.genusEngName})" else bird.genusEngName,
            )
            rows.forEachIndexed { idx, (label, value) ->
                if (value.isNotBlank()) {
                    if (idx > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(WildexDimens.borderStrokeChunky / 2)
                                .background(WildexTheme.extraColors.cartridgeOutline.copy(alpha = 0.3f)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.3f),
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BirdInfoDataRow(label: String, value: String) {
    val depth = WildexDimens.shadowOffsetHard
    Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
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
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Composable
private fun BirdInfoErrorState(
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
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun BirdInfoScreenPreview() {
    WildexTheme {
        BirdInfoContent(
            bird = BirdDetail(
                speciesId = "NNABR0000001",
                specimenNo = "S001",
                name = "황조롱이",
                scientificName = "Falco tinnunculus",
                phylumEngName = "Chordata",
                phylumName = "척삭동물문",
                classEngName = "Aves",
                className = "조강",
                orderEngName = "Falconiformes",
                orderName = "매목",
                familyEngName = "Falconidae",
                familyName = "매과",
                genusEngName = "Falco",
                genusName = "매속",
                generalFeature = "소형 맹금류로 우리나라 전역에 서식한다. 수컷은 머리가 회청색이고 등과 날개 윗면은 적갈색 바탕에 검은색 반점이 있다.",
                ecologicalFeature = "농경지, 초지, 하천변 등 개방된 환경을 선호하며 작은 설치류, 곤충, 소형 조류 등을 먹는다.",
                imageUrl = "",
                copyright = "",
            ),
            isSpeaking = false,
            onTtsClick = {},
        )
    }
}
