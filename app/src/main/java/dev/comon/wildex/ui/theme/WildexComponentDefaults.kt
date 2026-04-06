package dev.comon.wildex.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 스펙 시트·DESIGN.md에 맞춘 버튼·입력 기본값.
 * 화면에서는 색상/두께를 직접 쓰지 않고 이 객체를 우선 사용합니다.
 */
object WildexButtonDefaults {
    @Composable
    fun primaryColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun secondaryColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun invertedColors() = ButtonDefaults.buttonColors(
        containerColor = WildexTheme.extraColors.invertedButtonBackground,
        contentColor = WildexTheme.extraColors.invertedButtonContent,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun outlinedColors() = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.Transparent,
        contentColor = WildexTheme.extraColors.invertedButtonBackground,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun chunkyOutlineBorder(): BorderStroke = BorderStroke(
        width = WildexDimens.borderStrokeChunky,
        color = WildexTheme.extraColors.cartridgeOutline,
    )
}

object WildexInputDefaults {
    /**
     * DESIGN.md: 기본은 surface_container_low + on_surface 보더, 포커스 시 primary + surface_container_lowest 배경.
     */
    @Composable
    fun outlinedFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedContainerColor = WildexTheme.extraColors.surfaceContainerLowest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
