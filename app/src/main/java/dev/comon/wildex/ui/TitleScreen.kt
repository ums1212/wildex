package dev.comon.wildex.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import dev.comon.wildex.ui.theme.WildexPalette
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.comon.wildex.component.WildexCartridgePressButton
import dev.comon.wildex.ui.theme.WildexColorRoles

/** 타이틀 배경용 `res/raw/{이름}.webm` 베이스 이름(`R.raw` 대신 getIdentifier 사용). */
private const val TitleWebmRawName = "title_9_16"
private const val TitleWebmDarkRawName = "title_dark_9_16"

/**
 * 로그인 전 타이틀 화면. 테마에 따라 `res/raw/title_9_16.webm` 또는
 * `res/raw/title_dark_9_16.webm` 웹엠 루프를 재생합니다.
 *
 * @param onLoginClick 로그인·시작(중앙 패널 터치).
 * @param onGuideClick 가이드 버튼.
 */
@Composable
fun TitleScreen(
    onLoginClick: () -> Unit,
    onGuideClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    userNickname: String = "TRAINER_L_10",
    isDarkTheme: Boolean = false,
) {
    val inspection = LocalInspectionMode.current
    val titleVideoRawName = if (isDarkTheme) TitleWebmDarkRawName else TitleWebmRawName
    Box(modifier = modifier.fillMaxSize()) {
        if (inspection) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            )
        } else {
            TitleLoopingWebmBackground(
                rawName = titleVideoRawName,
                modifier = Modifier.fillMaxSize(),
            )
        }

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            TitleScreenScrollableLayout(
                containerMaxWidth = maxWidth,
                containerMaxHeight = maxHeight,
                isDarkTheme = isDarkTheme,
                userNickname = userNickname,
                onLoginClick = onLoginClick,
                onGuideClick = onGuideClick,
            )
        }
    }
}

@Composable
private fun TitleScreenScrollableLayout(
    containerMaxWidth: Dp,
    containerMaxHeight: Dp,
    isDarkTheme: Boolean,
    userNickname: String,
    onLoginClick: () -> Unit,
    onGuideClick: () -> Unit,
) {
    val landscape = containerMaxWidth > containerMaxHeight
    val scroll = rememberScrollState()
    val panelShadow = WildexDimens.gridStep * 2
    val contentPad = WildexDimens.gridMajor

    val titleStyle =
        if (landscape && containerMaxHeight < 420.dp) {
            MaterialTheme.typography.displaySmall
        } else {
            MaterialTheme.typography.displayMedium
        }

    val underlineWidth =
        if (landscape) {
            containerMaxWidth.coerceAtMost(280.dp)
        } else {
            containerMaxWidth.coerceAtMost(320.dp)
        }

    @Composable
    fun TitleBlock(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WildexTitleWithUnderline(
                titleStyle = titleStyle,
                underlineWidth = underlineWidth,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    @Composable
    fun CentralAndGuide(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentPad),
        ) {
            TitleCentralPanel(
                shadowOffset = panelShadow,
                onLoginClick = onLoginClick,
                landscapeCompact = landscape && containerMaxHeight < 480.dp,
            )
            GuideBar(onClick = onGuideClick)
        }
    }

    if (landscape) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = containerMaxHeight)
                    .padding(horizontal = contentPad, vertical = WildexDimens.gridStep * 2),
                horizontalArrangement = Arrangement.spacedBy(
                    contentPad,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TrainerBadge(
                        userNickname = userNickname,
                        shadowOffset = WildexDimens.shadowOffsetHard,
                        modifier = Modifier.padding(bottom = contentPad),
                    )
                    TitleBlock()
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CentralAndGuide(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = containerMaxHeight)
                    .padding(contentPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    contentPad,
                    Alignment.CenterVertically,
                ),
            ) {
                TrainerBadge(
                    userNickname = userNickname,
                    shadowOffset = WildexDimens.shadowOffsetHard,
                )
                TitleBlock()
                CentralAndGuide(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TitleLoopingWebmBackground(
    rawName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember(context, rawName) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }
    DisposableEffect(lifecycleOwner, player, rawName) {
        val rawId = context.resources.getIdentifier(rawName, "raw", context.packageName)
        require(rawId != 0) {
            "res/raw/${rawName}.webm 이 없습니다. 파일명·폴더를 확인하세요."
        }
        val uri = "android.resource://${context.packageName}/$rawId".toUri()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.play()
                Lifecycle.Event.ON_STOP -> player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            player.play()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        },
        update = { it.player = player },
        modifier = modifier,
    )
}

@Composable
private fun TrainerBadge(
    userNickname: String,
    shadowOffset: Dp,
    modifier: Modifier = Modifier,
) {
    val outline = WildexTheme.extraColors.cartridgeOutline
    val redShadow = MaterialTheme.colorScheme.primary

    Box(
        modifier
            .wrapContentSize()
            .padding(end = shadowOffset, bottom = shadowOffset),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(redShadow),
        )
        Row(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                .background(MaterialTheme.colorScheme.surface, RectangleShape)
                .padding(
                    horizontal = WildexDimens.gridMajor,
                    vertical = WildexDimens.gridStep * 2,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep * 2),
        ) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = userNickname,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WildexTitleWithUnderline(
    titleStyle: TextStyle,
    underlineWidth: Dp,
    isDarkTheme: Boolean,
) {
    val hard =
        if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface
        } else {
            WildexTheme.extraColors.cartridgeHardShadow
        }
    val accent = MaterialTheme.colorScheme.primary
    val outline = WildexTheme.extraColors.cartridgeOutline
    val lift = WildexDimens.shadowOffsetHard

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "WILDEX",
                style = titleStyle.copy(fontWeight = FontWeight.Bold),
                color = hard,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(lift, lift),
            )
            Text(
                text = "WILDEX",
                style = titleStyle.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))
        Box(
            Modifier
                .width(underlineWidth)
                .padding(end = lift, bottom = lift),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(WildexDimens.borderStrokeChunky)
                    .offset(lift, lift)
                    .background(hard),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(WildexDimens.borderStrokeChunky)
                    .border(WildexDimens.gridStep / 4, outline, RectangleShape)
                    .background(accent),
            )
        }
    }
}

@Composable
private fun TitleCentralPanel(
    shadowOffset: Dp,
    onLoginClick: () -> Unit,
    landscapeCompact: Boolean,
) {
    val outline = WildexTheme.extraColors.cartridgeOutline
    val hard = WildexTheme.extraColors.cartridgeHardShadow
    var innerH by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val pad = if (landscapeCompact) WildexDimens.gridStep * 3 else WildexDimens.gridMajor

    Box(
        Modifier
            .fillMaxWidth()
            .padding(end = shadowOffset, bottom = shadowOffset),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .offset(shadowOffset, shadowOffset)
                .fillMaxWidth(0.92f)
                .height(innerH)
                .background(hard),
        )
        Box(
            Modifier
                .fillMaxWidth(0.92f)
                .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                .background(MaterialTheme.colorScheme.surface, RectangleShape)
                .onGloballyPositioned {
                    innerH = with(density) { it.size.height.toDp() }
                },
        ) {
            PanelBrackets(
                color = MaterialTheme.colorScheme.primary,
                stroke = WildexDimens.borderStrokeChunky,
                inset = WildexDimens.gridStep * 2,
                modifier = Modifier.matchParentSize(),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    if (landscapeCompact) WildexDimens.gridStep * 2 else WildexDimens.gridMajor,
                ),
            ) {
                LogInBlock(
                    landscapeCompact = landscapeCompact,
                    onLoginClick = onLoginClick,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(WildexDimens.gridStep * 2),
                ) {
                    Text(
                        text = "TOUCH TO START",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 4.sp,
                        ),
                        color = WildexPalette.SecondaryMuted,
                        textAlign = TextAlign.Center,
                    )
                    PageDots()
                }
            }
        }
    }
}

@Composable
private fun PanelBrackets(
    color: androidx.compose.ui.graphics.Color,
    stroke: Dp,
    inset: Dp,
    modifier: Modifier = Modifier,
) {
    val arm = WildexDimens.gridMajor + WildexDimens.gridStep
    fun Modifier.cornerTopStart() = this.padding(start = inset, top = inset)
    fun Modifier.cornerTopEnd() = this.padding(end = inset, top = inset)
    fun Modifier.cornerBottomStart() = this.padding(start = inset, bottom = inset)
    fun Modifier.cornerBottomEnd() = this.padding(end = inset, bottom = inset)

    Box(modifier) {
        Box(Modifier.cornerTopStart().align(Alignment.TopStart).width(arm).height(stroke).background(color))
        Box(Modifier.cornerTopStart().align(Alignment.TopStart).width(stroke).height(arm).background(color))
        Box(Modifier.cornerTopEnd().align(Alignment.TopEnd).width(arm).height(stroke).background(color))
        Box(Modifier.cornerTopEnd().align(Alignment.TopEnd).width(stroke).height(arm).background(color))
        Box(Modifier.cornerBottomStart().align(Alignment.BottomStart).width(arm).height(stroke).background(color))
        Box(Modifier.cornerBottomStart().align(Alignment.BottomStart).width(stroke).height(arm).background(color))
        Box(Modifier.cornerBottomEnd().align(Alignment.BottomEnd).width(arm).height(stroke).background(color))
        Box(Modifier.cornerBottomEnd().align(Alignment.BottomEnd).width(stroke).height(arm).background(color))
    }
}

@Composable
private fun LogInBlock(
    landscapeCompact: Boolean,
    onLoginClick: () -> Unit,
) {
    var navigating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val style =
        if (landscapeCompact) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.titleLarge
        }
    WildexCartridgePressButton(
        onClick = {
            if (navigating) return@WildexCartridgePressButton
            scope.launch {
                navigating = true
                delay(120)
                onLoginClick()
            }
        },
        enabled = !navigating,
        backgroundColor = WildexColorRoles.missionCtaBackground(),
        frameColor = WildexTheme.extraColors.cartridgeOutline,
        shadowBlockColor = WildexTheme.extraColors.cartridgeHardShadow,
    ) {
        Text(
            text = "LOG IN",
            style = style.copy(fontWeight = FontWeight.Bold),
            color = WildexColorRoles.missionCtaForeground(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageDots() {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            tick++
        }
    }
    val activeIndex = tick % 3

    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.surfaceContainerHighest
    val outline = WildexTheme.extraColors.cartridgeOutline
    Row(
        horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep * 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            Box(
                Modifier
                    .size(WildexDimens.gridStep * 3)
                    .border(WildexDimens.gridStep / 4, outline, RectangleShape)
                    .background(if (i == activeIndex) active else idle),
            )
        }
    }
}

@Composable
private fun GuideBar(onClick: () -> Unit) {
    WildexCartridgePressButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        frameColor = WildexTheme.extraColors.cartridgeOutline,
        shadowBlockColor = WildexTheme.extraColors.shadowMass,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(end = WildexDimens.gridStep * 2)
                .size(22.dp),
        )
        Text(
            text = "GUIDE",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun TitleScreenPreviewPortrait() {
    WildexTheme {
        TitleScreen(onLoginClick = { })
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun TitleScreenPreviewLandscape() {
    WildexTheme {
        TitleScreen(onLoginClick = { })
    }
}
