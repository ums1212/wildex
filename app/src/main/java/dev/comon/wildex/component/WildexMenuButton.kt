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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexPalette
import dev.comon.wildex.ui.theme.WildexTheme
import java.util.Locale

/**
 * 메인 메뉴용 카트리지 스타일 버튼: 직각 두꺼운 보더 + 하드 오프셋 프레임(DESIGN.md).
 *
 * 배경·아이콘 영역·텍스트 색은 호출부에서 바꿀 수 있습니다.
 */
@Composable
fun WildexMenuButton(
    titleText: String,
    subtitleText: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    iconBackgroundColor: Color = WildexTheme.extraColors.surfaceContainerHighest,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    frameColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowBlockColor: Color = WildexTheme.extraColors.shadowMass,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
) {
    WildexMenuButtonImpl(
        titleText = titleText,
        subtitleText = subtitleText,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor,
        iconBackgroundColor = iconBackgroundColor,
        titleTextColor = titleTextColor,
        subtitleTextColor = subtitleTextColor,
        frameColor = frameColor,
        shadowBlockColor = shadowBlockColor,
        enabled = enabled,
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = iconContentDescription ?: titleText,
                tint = iconTintColor,
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
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    iconBackgroundColor: Color = WildexTheme.extraColors.surfaceContainerHighest,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    frameColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowBlockColor: Color = WildexTheme.extraColors.shadowMass,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
) {
    WildexMenuButtonImpl(
        titleText = titleText,
        subtitleText = subtitleText,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor,
        iconBackgroundColor = iconBackgroundColor,
        titleTextColor = titleTextColor,
        subtitleTextColor = subtitleTextColor,
        frameColor = frameColor,
        shadowBlockColor = shadowBlockColor,
        enabled = enabled,
        icon = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = iconContentDescription ?: titleText,
                tint = iconTintColor,
                modifier = Modifier.size(28.dp),
            )
        },
    )
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
    icon: @Composable () -> Unit,
) {
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
                        onClick = onClick,
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
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
private fun WildexMenuButtonPreviewGrid() {
    WildexTheme {
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
                backgroundColor = WildexPalette.Primary,
                iconBackgroundColor = WildexPalette.SurfaceContainerLowest,
                iconTintColor = WildexPalette.Primary,
                titleTextColor = WildexPalette.OnPrimary,
                subtitleTextColor = WildexPalette.OnPrimary.copy(alpha = 0.92f),
                frameColor = WildexPalette.OnSurface,
                shadowBlockColor = WildexPalette.OnSurface,
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
                    backgroundColor = WildexPalette.SurfaceContainerLowest,
                    iconBackgroundColor = WildexPalette.SurfaceContainerHighest,
                    iconTintColor = WildexPalette.OnSurface,
                    titleTextColor = WildexPalette.OnSurface,
                    subtitleTextColor = WildexPalette.SecondaryMuted,
                    frameColor = WildexPalette.OnSurface,
                    shadowBlockColor = WildexPalette.OnSurface,
                )
                WildexMenuButton(
                    titleText = "Settings",
                    subtitleText = "Configure",
                    imageVector = Icons.Filled.Settings,
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    backgroundColor = WildexPalette.SurfaceContainerLowest,
                    iconBackgroundColor = WildexPalette.SurfaceContainerHighest,
                    iconTintColor = WildexPalette.OnSurface,
                    titleTextColor = WildexPalette.OnSurface,
                    subtitleTextColor = WildexPalette.SecondaryMuted,
                    frameColor = WildexPalette.OnSurface,
                    shadowBlockColor = WildexPalette.OnSurface,
                )
            }
        }
    }
}
