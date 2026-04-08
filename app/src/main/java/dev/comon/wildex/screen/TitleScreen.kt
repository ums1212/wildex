package dev.comon.wildex.screen

import android.content.res.Configuration
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
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
import androidx.media3.common.util.UnstableApi
import dev.comon.wildex.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.comon.wildex.component.WildexCartridgePressButton
import dev.comon.wildex.ui.theme.WildexColorRoles

/**
 * 로그인 전 타이틀 화면. 다크테마 여부에 따라 `res/raw/title_9_16.webm` 또는
 * `res/raw/title_dark_9_16.webm` 웹엠 루프를 재생합니다.
 *
 * @param onLoginClick 로그인·시작(중앙 패널 터치).
 * @param onGuideClick 가이드 버튼.
 */
@Composable
fun TitleScreen(
    isDarkTheme: Boolean,
    onLoginClick: () -> Unit,
    onGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
    userNickname: String = "TRAINER_L_10",
) {
    val inspection = LocalInspectionMode.current

    Box(modifier = modifier.fillMaxSize()) {
        if(!inspection){
            TitleLoopingWebmBackground(
                isDarkTheme = isDarkTheme,
                modifier = Modifier.fillMaxSize(),
            )
        }

        TitleScreenScrollableLayout(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            isDarkTheme = isDarkTheme,
            userNickname = userNickname,
            onLoginClick = onLoginClick,
            onGuideClick = onGuideClick,
        )
    }
}

@Composable
private fun TitleScreenScrollableLayout(
    isDarkTheme: Boolean,
    userNickname: String,
    onLoginClick: () -> Unit,
    onGuideClick: () -> Unit,
    modifier: Modifier = Modifier, // modifier 추가
) {
    // 현재 기기의 화면 구성 정보를 가져옵/니다.
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val scroll = rememberScrollState()
    val panelShadow = WildexDimens.gridStep * 2
    val contentPad = WildexDimens.gridMajor

    // 1. 타이틀 스타일 결정 (기존 로직 유지)
    val titleStyle = if (isLandscape && screenHeight < 420.dp) {
        MaterialTheme.typography.displayMedium
    } else {
        MaterialTheme.typography.displayLarge
    }

    // 2. 밑줄 너비 결정
    val underlineWidth = if (isLandscape) {
        screenWidth.coerceAtMost(280.dp)
    } else {
        screenWidth.coerceAtMost(320.dp)
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
                landscapeCompact = isLandscape && screenHeight < 480.dp,
            )
            GuideBar(onClick = onGuideClick)
        }
    }

    // 레이아웃 구성
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll),
    ) {
        if (isLandscape) {
            // 가로 모드 레이아웃
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight) // 전체 높이를 확보하여 중앙 정렬 유지
                    .padding(horizontal = contentPad, vertical = WildexDimens.gridStep * 2),
                horizontalArrangement = Arrangement.spacedBy(contentPad, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CentralAndGuide(modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            // 세로 모드 레이아웃
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight)
                    .padding(contentPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(contentPad, Alignment.CenterVertically),
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

@OptIn(UnstableApi::class)
@Composable
private fun TitleLoopingWebmBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val titleVideoRawInt = if (isDarkTheme) R.raw.title_dark_9_16 else R.raw.title_9_16
    var isVideoReady by remember { mutableStateOf(false) }

    val player = remember(context, titleVideoRawInt) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL

            addListener(object: Player.Listener{
                override fun onRenderedFirstFrame() {
                    super.onRenderedFirstFrame()
                    isVideoReady = true
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, player, titleVideoRawInt) {
        val uri = "android.resource://${context.packageName}/$titleVideoRawInt".toUri()
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

    Box {
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
        if(!isVideoReady){
            val staticBackground = if(isDarkTheme) R.drawable.title_background_dark else R.drawable.title_background
            Image(
                painter = painterResource(staticBackground),
                contentDescription = "background image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // RESIZE_MODE_ZOOM과 맞춤
            )
        }
    }

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
    color: Color,
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
        modifier = Modifier.fillMaxWidth(0.92f),
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
        TitleScreen(onLoginClick = { }, onGuideClick = { }, isDarkTheme = false)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun TitleScreenPreviewLandscape() {
    WildexTheme {
        TitleScreen(onLoginClick = { }, onGuideClick = { }, isDarkTheme = false)
    }
}
