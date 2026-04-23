package dev.comon.wildex.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import kotlinx.coroutines.delay

@Composable
fun WildexClickableCard(
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    verticalAlignment: androidx.compose.ui.Alignment.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    frameColor: Color = WildexTheme.extraColors.cartridgeOutline,
    shadowBlockColor: Color = WildexTheme.extraColors.cartridgeHardShadow,
    content: @Composable RowScope.() -> Unit,
) {
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(shadowBlockColor, RectangleShape),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(contentInset, contentInset)
                .border(WildexDimens.borderStrokeChunky, frameColor, RectangleShape)
                .background(backgroundColor, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(contentPadding),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            content = content,
        )
    }
}
