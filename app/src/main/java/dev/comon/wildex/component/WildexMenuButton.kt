package dev.comon.wildex.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.util.Locale

/**
 * 메인 메뉴 등에서 쓰는 사전 정의 색 조합. [MaterialTheme]·[WildexTheme.extraColors]에 맞춰 다크/라이트에 대응합니다.
 * [WildexMenuButton]에 지정하면 아래 색 파라미터는 무시됩니다.
 */
enum class WildexMenuButtonStyle {
    /** 브랜드 레드 면 + 아이콘 웰(Capture 등 주요 액션). */
    Primary,
    /** 카트리지형 중립 면(Journal / Settings 등). */
    Secondary,
}

private data class WildexMenuButtonColorSet(
    val backgroundColor: Color,
    val iconBackgroundColor: Color,
    val iconTintColor: Color,
    val titleTextColor: Color,
    val subtitleTextColor: Color,
    val frameColor: Color,
    val shadowBlockColor: Color,
)

@Composable
private fun wildexMenuButtonColorSet(style: WildexMenuButtonStyle): WildexMenuButtonColorSet = when (style) {
    WildexMenuButtonStyle.Primary -> WildexMenuButtonColorSet(
        backgroundColor = WildexColorRoles.missionCtaBackground(),
        iconBackgroundColor = WildexTheme.extraColors.surfaceContainerHighest,
        iconTintColor = WildexColorRoles.missionCtaIconAccent(),
        titleTextColor = WildexColorRoles.missionCtaForeground(),
        subtitleTextColor = WildexColorRoles.missionCtaForeground().copy(alpha = 0.92f),
        frameColor = WildexTheme.extraColors.cartridgeOutline,
        shadowBlockColor = WildexTheme.extraColors.cartridgeHardShadow,
    )
    WildexMenuButtonStyle.Secondary -> WildexMenuButtonColorSet(
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        iconBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        iconTintColor = MaterialTheme.colorScheme.onSurface,
        titleTextColor = MaterialTheme.colorScheme.onSurface,
        subtitleTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        frameColor = WildexTheme.extraColors.cartridgeOutline,
        shadowBlockColor = WildexTheme.extraColors.shadowMass,
    )
}

/**
 * 메인 메뉴용 카트리지 스타일 버튼: 직각 두꺼운 보더 + 하드 오프셋 프레임(DESIGN.md).
 *
 * [style]이 null이면 [backgroundColor] 등 개별 색을 사용하고, 지정 시 현재 테마에 맞는 조합으로 위 색들을 덮어씁니다.
 */
@Composable
fun WildexMenuButton(
    titleText: String,
    subtitleText: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WildexMenuButtonStyle? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    iconBackgroundColor: Color = WildexTheme.extraColors.surfaceContainerHighest,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    frameColor: Color = WildexTheme.extraColors.cartridgeOutline,
    shadowBlockColor: Color = WildexTheme.extraColors.shadowMass,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues,
) {
    val colors = if (style != null) {
        wildexMenuButtonColorSet(style)
    } else {
        WildexMenuButtonColorSet(
            backgroundColor = backgroundColor,
            iconBackgroundColor = iconBackgroundColor,
            iconTintColor = iconTintColor,
            titleTextColor = titleTextColor,
            subtitleTextColor = subtitleTextColor,
            frameColor = frameColor,
            shadowBlockColor = shadowBlockColor,
        )
    }
    WildexMenuButtonImpl(
        titleText = titleText,
        subtitleText = subtitleText,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = colors.backgroundColor,
        iconBackgroundColor = colors.iconBackgroundColor,
        titleTextColor = colors.titleTextColor,
        subtitleTextColor = colors.subtitleTextColor,
        frameColor = colors.frameColor,
        shadowBlockColor = colors.shadowBlockColor,
        enabled = enabled,
        contentPadding = contentPadding,
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = iconContentDescription ?: titleText,
                tint = colors.iconTintColor,
                modifier = Modifier.size(28.dp),
            )
        },
    )
}

@Composable
fun WildexMenuButton(
    titleText: String,
    subtitleText: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WildexMenuButtonStyle? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    iconBackgroundColor: Color = WildexTheme.extraColors.surfaceContainerHighest,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    frameColor: Color = WildexTheme.extraColors.cartridgeOutline,
    shadowBlockColor: Color = WildexTheme.extraColors.shadowMass,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues,
) {
    val colors = if (style != null) {
        wildexMenuButtonColorSet(style)
    } else {
        WildexMenuButtonColorSet(
            backgroundColor = backgroundColor,
            iconBackgroundColor = iconBackgroundColor,
            iconTintColor = iconTintColor,
            titleTextColor = titleTextColor,
            subtitleTextColor = subtitleTextColor,
            frameColor = frameColor,
            shadowBlockColor = shadowBlockColor,
        )
    }
    WildexMenuButtonImpl(
        titleText = titleText,
        subtitleText = subtitleText,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = colors.backgroundColor,
        iconBackgroundColor = colors.iconBackgroundColor,
        titleTextColor = colors.titleTextColor,
        subtitleTextColor = colors.subtitleTextColor,
        frameColor = colors.frameColor,
        shadowBlockColor = colors.shadowBlockColor,
        enabled = enabled,
        contentPadding = contentPadding,
        icon = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = iconContentDescription ?: titleText,
                tint = colors.iconTintColor,
                modifier = Modifier.size(28.dp),
            )
        },
    )
}

/**
 * [WildexMenuButton]과 같은 하드 섀도·눌림 시 면 인셋(interactionSource + [collectIsPressedAsState]).
 * 로그인·가이드 등 텍스트/아이콘 단일 줄 액션용.
 */
@Composable
fun WildexCartridgePressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    frameColor: Color = WildexTheme.extraColors.cartridgeOutline,
    shadowBlockColor: Color = WildexTheme.extraColors.cartridgeHardShadow,
    horizontalPadding: Dp = WildexDimens.gridMajor,
    verticalPadding: Dp = WildexDimens.gridStep * 3,
    content: @Composable RowScope.() -> Unit,
) {
    val debouncedOnClick = rememberDebounceClick(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depthNormal = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val shadowOffset = if (pressed) depthPressed else depthNormal
    val contentInset = if (pressed) depthNormal - depthPressed else 0.dp
    val shape = RectangleShape

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = depthNormal, bottom = depthNormal)
    ) {
        // 2. 그림자 영역 (Row의 크기에 맞춰짐)
        Box(
            modifier = Modifier
                .matchParentSize() // 이제 Row가 크기를 결정하면 그에 맞춰집니다.
                .offset(shadowOffset, shadowOffset)
                .background(shadowBlockColor, shape)
        )

        // 3. 메인 콘텐츠 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (pressed) Modifier.offset(contentInset, contentInset)
                    else Modifier
                )
                .background(backgroundColor, shape)
                .border(WildexDimens.borderStrokeChunky, frameColor, shape)
                .clip(shape) // 테두리 안쪽으로 클릭 효과 등을 제한
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = debouncedOnClick
                )
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * 원형 카트리지 스타일 프레스 버튼. [WildexCartridgePressButton]과 동일한 하드 섀도·눌림 효과를
 * [CircleShape]에 적용합니다.
 *
 * @param isActive 외부에서 제어하는 활성(토글) 상태. `true`일 때 눌린 것과 동일하게 섀도가 줄어듭니다.
 * @param buttonModifier 버튼 본체(원)에 추가할 Modifier. [drawBehind] 등 원 바깥에 그리는 효과를 넣을 때 활용합니다.
 * @param content 버튼 중앙에 배치되는 슬롯.
 */
@Composable
fun WildexCircleCartridgePressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    isActive: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    frameColor: Color = WildexTheme.extraColors.cartridgeOutline,
    shadowBlockColor: Color = WildexTheme.extraColors.cartridgeHardShadow,
    buttonModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val debouncedOnClick = rememberDebounceClick(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depthNormal = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val isDown = pressed || isActive
    val shadowOffset = if (isDown) depthPressed else depthNormal
    val contentInset = if (isDown) depthNormal - depthPressed else 0.dp

    Box(
        modifier = modifier.padding(end = depthNormal, bottom = depthNormal),
        contentAlignment = Alignment.Center,
    ) {
        // 하드 섀도
        Box(
            modifier = Modifier
                .size(size)
                .offset(shadowOffset, shadowOffset)
                .background(shadowBlockColor, CircleShape),
        )
        // 버튼 본체
        Box(
            modifier = Modifier
                .size(size)
                .then(if (isDown) Modifier.offset(contentInset, contentInset) else Modifier)
                .then(buttonModifier)
                .border(WildexDimens.borderStrokeChunky, frameColor, CircleShape)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = debouncedOnClick,
                ),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
private fun WildexMenuButtonImpl(
    titleText: String,
    subtitleText: String,
    onClick: () -> Unit,
    modifier: Modifier,
    backgroundColor: Color,
    iconBackgroundColor: Color,
    titleTextColor: Color,
    subtitleTextColor: Color,
    frameColor: Color,
    shadowBlockColor: Color,
    enabled: Boolean,
    contentPadding: PaddingValues,
    icon: @Composable () -> Unit,
) {
    val debouncedOnClick = rememberDebounceClick(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val depthNormal = WildexDimens.shadowOffsetHard
    /** 눌림 시 남기는 얇은 하드 섀도(스크린샷의 pressed 상태) */
    val depthPressed = 2.dp
    val shadowOffset = if (pressed) depthPressed else depthNormal
    /** 두꺼운 섀도만큼 내려앉히되, 얇게 남긴 섀도 두께는 비워 둠 */
    val contentInset = if (pressed) depthNormal - depthPressed else 0.dp
    val shape = RectangleShape

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = depthNormal, bottom = depthNormal),
    ) {
        // 프리뷰 등은 maxHeight만 큰 유한값으로 주는 경우가 많아 Infinity 비교만으로는 부족함.
        // Row + fillMaxHeight처럼 세로가 확정(min==max)일 때만 셀을 세로로 채움.
        // 명시적 this로 BoxWithConstraintsScope 사용을 린트/분석기가 인식하도록 함.
        val columnVerticalFill =
            if (this.constraints.hasFixedHeight) {
                Modifier.fillMaxHeight()
            } else {
                Modifier
            }
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(shadowOffset, shadowOffset)
                    .background(shadowBlockColor, shape),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(columnVerticalFill)
                    .then(
                        if (pressed) {
                            Modifier.offset(contentInset, contentInset)
                        } else {
                            Modifier
                        },
                    )
                    .clip(shape)
                    .border(WildexDimens.borderStrokeChunky, frameColor, shape)
                    .background(backgroundColor, shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = debouncedOnClick,
                    )
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(WildexDimens.borderStrokeChunky, frameColor, shape)
                        .background(iconBackgroundColor, shape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    icon()
                }
                Text(
                    text = titleText.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = subtitleText.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(color = subtitleTextColor),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun WildexMenuButtonPreviewGridLight() {
    WildexTheme(darkTheme = false) {
        WildexMenuButtonPreviewGridContent()
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WildexMenuButtonPreviewGridDark() {
    WildexTheme(darkTheme = true) {
        WildexMenuButtonPreviewGridContent()
    }
}

@Composable
private fun WildexMenuButtonPreviewGridContent() {
    val previewPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WildexMenuButton(
            titleText = "Capture",
            subtitleText = "Scan new specimen",
            imageVector = Icons.Filled.CameraAlt,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            style = WildexMenuButtonStyle.Primary,
            contentPadding = previewPadding,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WildexMenuButton(
                titleText = "Collection",
                subtitleText = "Findings",
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                style = WildexMenuButtonStyle.Secondary,
                contentPadding = previewPadding,
            )
            WildexMenuButton(
                titleText = "Settings",
                subtitleText = "Configure",
                imageVector = Icons.Filled.Settings,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                style = WildexMenuButtonStyle.Secondary,
                contentPadding = previewPadding,
            )
        }
    }
}
