package dev.comon.wildex.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.util.Locale

/**
 * 레트로 카트리지 스타일 로그아웃 확인 다이얼로그 (두꺼운 보더·하드 섀도우·헤더 바·할프톤 바디).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildexLogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit,
    titleText: String = "로그아웃?",
    messageText: String = "로그아웃하고 타이틀 화면으로 돌아가시겠습니까?",
    confirmText: String = "로그아웃",
    dismissText: String? = "취소",
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            WildexLogoutConfirmDialogCard(
                onDismiss = onDismiss,
                onConfirmLogout = onConfirmLogout,
                titleText = titleText,
                messageText = messageText,
                confirmText = confirmText,
                dismissText = dismissText,
            )
        }
    }
}

@Composable
private fun WildexLogoutConfirmDialogCard(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit,
    titleText: String,
    messageText: String,
    confirmText: String,
    dismissText: String?,
    modifier: Modifier = Modifier,
) {
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val hardShadow = WildexTheme.extraColors.cartridgeHardShadow
    val bodyInk = MaterialTheme.colorScheme.onSurface
    val cardSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBodySurface = MaterialTheme.colorScheme.surfaceVariant
    val dismissFill = MaterialTheme.colorScheme.surfaceContainerLowest
    val missionBg = WildexColorRoles.missionCtaBackground()
    val missionFg = WildexColorRoles.missionCtaForeground()

    Box(
        modifier = modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(hardShadow, RectangleShape),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                .background(cardSurface, RectangleShape),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(missionBg, RectangleShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "경고",
                        tint = missionFg,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = titleText.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = missionFg,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WildexDimens.borderStrokeChunky)
                        .background(outline, RectangleShape),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBodySurface, RectangleShape)
                        .drawBehind {
                            val spacing = 5.dp.toPx()
                            val r = 0.9.dp.toPx()
                            val dot = outline.copy(alpha = 0.14f)
                            var py = spacing * 0.5f
                            while (py < size.height) {
                                var px = spacing * 0.5f
                                while (px < size.width) {
                                    drawCircle(
                                        color = dot,
                                        radius = r,
                                        center = Offset(px, py),
                                    )
                                    px += spacing
                                }
                                py += spacing
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = messageText.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = bodyInk,
                        textAlign = TextAlign.Center,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBodySurface, RectangleShape)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WildexLogoutDialogActionButton(
                        text = confirmText,
                        onClick = {
                            onConfirmLogout()
                            onDismiss()
                        },
                        containerColor = missionBg,
                        contentColor = missionFg,
                    )
                    if (dismissText != null) {
                        WildexLogoutDialogActionButton(
                            text = dismissText,
                            onClick = onDismiss,
                            containerColor = dismissFill,
                            contentColor = bodyInk,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset((-WildexDimens.gridStep), (-WildexDimens.gridStep))
                    .size(12.dp)
                    .zIndex(1f)
                    .background(outline, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(cardSurface, RectangleShape),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(WildexDimens.gridStep, WildexDimens.gridStep)
                    .size(12.dp)
                    .zIndex(1f)
                    .background(outline, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(cardSurface, RectangleShape),
                )
            }
        }
    }
}

@Composable
private fun WildexLogoutDialogActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
) {
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val hardShadow = WildexTheme.extraColors.cartridgeHardShadow
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = depth, bottom = depth),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(depth, depth)
                    .background(hardShadow, RectangleShape),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                    .background(containerColor, RectangleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "로그아웃 다이얼로그")
@Composable
private fun WildexLogoutConfirmDialogPreview() {
    WildexTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            WildexLogoutConfirmDialogCard(
                onDismiss = { },
                onConfirmLogout = { },
                titleText = "로그아웃?",
                messageText = "로그아웃하고 타이틀 화면으로 돌아가시겠습니까?",
                confirmText = "로그아웃",
                dismissText = "취소",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}
