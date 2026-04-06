package dev.comon.wildex.ui

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import dev.comon.wildex.ui.theme.WildexButtonDefaults
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexInputDefaults
import dev.comon.wildex.ui.theme.WildexPalette
import dev.comon.wildex.ui.theme.WildexShapes
import dev.comon.wildex.ui.theme.WildexTheme

private fun colorHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

/**
 * 테마 스위치가 동작하도록 [WildexTheme]까지 포함한 진입점.
 */
@Composable
fun WildexDesignSystemHost() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    WildexTheme(darkTheme = darkTheme) {
        DesignSystemTestScreen(
            darkTheme = darkTheme,
            onDarkThemeChange = { darkTheme = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemTestScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val scheme = MaterialTheme.colorScheme
    val extra = WildexTheme.extraColors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "디자인 시스템 검증",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = scheme.secondary,
                tonalElevation = 0.dp,
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(scheme.primary, RoundedCornerShape(WildexDimens.radiusSmallSoft))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                            )
                        }
                    },
                    label = { Text("홈") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = scheme.onPrimary,
                        selectedTextColor = scheme.onSecondary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = scheme.onSecondary,
                        unselectedTextColor = scheme.onSecondary,
                    ),
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("검색") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("프로필") },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionTitle("테마")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("다크 모드", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
            }

            SectionTitle("Material colorScheme")
            ColorTokenRow("primary", scheme.primary)
            ColorTokenRow("onPrimary", scheme.onPrimary)
            ColorTokenRow("primaryContainer", scheme.primaryContainer)
            ColorTokenRow("secondary", scheme.secondary)
            ColorTokenRow("onSecondary", scheme.onSecondary)
            ColorTokenRow("tertiary", scheme.tertiary)
            ColorTokenRow("surface", scheme.surface)
            ColorTokenRow("onSurface", scheme.onSurface)
            ColorTokenRow("surfaceVariant", scheme.surfaceVariant)
            ColorTokenRow("onSurfaceVariant", scheme.onSurfaceVariant)
            ColorTokenRow("surfaceContainerLowest", scheme.surfaceContainerLowest)
            ColorTokenRow("surfaceContainerLow", scheme.surfaceContainerLow)
            ColorTokenRow("surfaceContainerHigh", scheme.surfaceContainerHigh)
            ColorTokenRow("surfaceContainerHighest", scheme.surfaceContainerHighest)
            ColorTokenRow("outline", scheme.outline)
            ColorTokenRow("outlineVariant", scheme.outlineVariant)
            ColorTokenRow("error", scheme.error)

            SectionTitle("WildexTheme.extraColors")
            ColorTokenRow("surfaceContainerLowest", extra.surfaceContainerLowest)
            ColorTokenRow("surfaceContainerHighest", extra.surfaceContainerHighest)
            ColorTokenRow("shadowMass", extra.shadowMass)
            ColorTokenRow("invertedButtonBackground", extra.invertedButtonBackground)
            ColorTokenRow("invertedButtonContent", extra.invertedButtonContent)
            ColorTokenRow("searchBarBorderFocused", extra.searchBarBorderFocused)

            SectionTitle("WildexPalette (정적 참조)")
            ColorTokenRow("SpecSheetPureRed", WildexPalette.SpecSheetPureRed)
            ColorTokenRow("Primary", WildexPalette.Primary)
            ColorTokenRow("SecondaryMuted", WildexPalette.SecondaryMuted)
            ColorTokenRow("OutlineVariant (raw)", WildexPalette.OutlineVariant)

            SectionTitle("타이포그래피 (WildexTypography)")
            Text("displayLarge", style = MaterialTheme.typography.displayLarge)
            Text("headlineSmall", style = MaterialTheme.typography.headlineSmall)
            Text("titleMedium · Public Sans", style = MaterialTheme.typography.titleMedium)
            Text("bodyMedium · 본문", style = MaterialTheme.typography.bodyMedium)
            Text("labelMedium · Space Grotesk", style = MaterialTheme.typography.labelMedium)

            SectionTitle("폰트 패밀리")
            Text(
                "display/headline/label → Space Grotesk",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                "title/body → Public Sans (이 문장)",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )

            SectionTitle("버튼 (WildexButtonDefaults)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, colors = WildexButtonDefaults.primaryColors()) {
                    Text("Primary")
                }
                Button(onClick = {}, colors = WildexButtonDefaults.secondaryColors()) {
                    Text("Secondary")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, colors = WildexButtonDefaults.invertedColors()) {
                    Text("Inverted")
                }
                OutlinedButton(
                    onClick = {},
                    border = WildexButtonDefaults.chunkyOutlineBorder(),
                    colors = WildexButtonDefaults.outlinedColors(),
                ) {
                    Text("Outlined")
                }
            }

            SectionTitle("입력 · 검색바 스타일")
            var query by remember { mutableStateOf("") }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = WildexInputDefaults.outlinedFieldColors(),
            )

            SectionTitle("선택 칩 (DESIGN.md)")
            var chipOn by remember { mutableStateOf(false) }
            FilterChip(
                selected = chipOn,
                onClick = { chipOn = !chipOn },
                label = { Text("필터") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = scheme.surfaceVariant,
                    labelColor = scheme.onSurface,
                    selectedContainerColor = scheme.onSurface,
                    selectedLabelColor = scheme.surface,
                ),
            )

            SectionTitle("쉐이프 · 보더")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapePreviewCard(
                    label = "카트리지\n0dp",
                    shape = WildexShapes.cartridge.medium,
                )
                ShapePreviewCard(
                    label = "스펙 시트\n12dp",
                    shape = WildexShapes.specSheet.medium,
                )
            }

            SectionTitle("하드 오프셋 섀도 (DESIGN.md)")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(end = WildexDimens.shadowOffsetHard, bottom = WildexDimens.shadowOffsetHard),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(WildexDimens.shadowOffsetHard, WildexDimens.shadowOffsetHard)
                        .background(extra.shadowMass),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(scheme.surfaceContainerHighest, MaterialTheme.shapes.medium)
                        .border(WildexDimens.borderStrokeChunky, scheme.outline, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        "${WildexDimens.borderStrokeChunky} 보더 + ${WildexDimens.shadowOffsetHard} 오프셋 블록",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ColorTokenRow(label: String, color: Color) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(6.dp))
                .border(1.dp, outline, RoundedCornerShape(6.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                colorHex(color),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShapePreviewCard(label: String, shape: androidx.compose.ui.graphics.Shape) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 56.dp)
                .background(scheme.surfaceContainerLowest, shape)
                .border(WildexDimens.borderStrokeChunky, scheme.outline, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DesignSystemTestScreenPreview() {
    WildexTheme {
        DesignSystemTestScreen(darkTheme = false, onDarkThemeChange = {})
    }
}
