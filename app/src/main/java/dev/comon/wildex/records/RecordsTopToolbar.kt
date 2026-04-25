package dev.comon.wildex.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.comon.wildex.component.rememberDebounceClick
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import androidx.compose.foundation.clickable

@Composable
fun RecordsTopToolbar(
    isDateFilterActive: Boolean,
    isSortAscending: Boolean,
    onCalendarClick: () -> Unit,
    onSortToggle: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WildexDimens.gridMajor, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarIconButton(
            icon = Icons.Filled.CalendarMonth,
            contentDescription = "날짜 필터",
            active = isDateFilterActive,
            onClick = onCalendarClick,
        )
        ToolbarIconButton(
            icon = if (isSortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = if (isSortAscending) "오름차순" else "내림차순",
            active = false,
            onClick = onSortToggle,
        )
        ToolbarIconButton(
            icon = Icons.Filled.Search,
            contentDescription = "검색",
            active = false,
            onClick = onSearchClick,
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val debouncedClick = rememberDebounceClick(onClick)
    val depth = WildexDimens.shadowOffsetHard
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowOffset = if (isPressed) 0.dp else depth
    val contentInset = if (isPressed) depth else 0.dp

    val bgColor = if (active) WildexColorRoles.missionCtaBackground()
    else MaterialTheme.colorScheme.surfaceContainerLowest
    val iconTint = if (active) WildexColorRoles.missionCtaForeground()
    else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
        )
        Box(
            modifier = Modifier
                .size(40.dp - depth)
                .offset(contentInset, contentInset)
                .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                .background(bgColor, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = debouncedClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
