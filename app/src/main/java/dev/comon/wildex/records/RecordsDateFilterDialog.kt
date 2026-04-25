package dev.comon.wildex.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import dev.comon.wildex.component.WildexDropdown
import dev.comon.wildex.component.rememberDebounceClick
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsDateFilterDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onApply: (Long) -> Unit,
    onClearAll: () -> Unit,
) {
    val now = Calendar.getInstance()
    val initCal = Calendar.getInstance().apply {
        timeInMillis = initialMillis ?: now.timeInMillis
    }
    val currentYear = now.get(Calendar.YEAR)
    val years = (currentYear downTo currentYear - 9).toList()

    var selectedYear by remember { mutableIntStateOf(initCal.get(Calendar.YEAR).coerceIn(years.last(), years.first())) }
    var selectedMonth by remember { mutableIntStateOf(initCal.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(initCal.get(Calendar.DAY_OF_MONTH)) }

    fun maxDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

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
            DateFilterCard(
                years = years,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                onYearChange = { y ->
                    selectedYear = y
                    selectedDay = selectedDay.coerceAtMost(maxDay(y, selectedMonth))
                },
                onMonthChange = { m ->
                    selectedMonth = m
                    selectedDay = selectedDay.coerceAtMost(maxDay(selectedYear, m))
                },
                onDayChange = { selectedDay = it },
                onApply = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, selectedYear)
                    cal.set(Calendar.MONTH, selectedMonth - 1)
                    cal.set(Calendar.DAY_OF_MONTH, selectedDay)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    onApply(cal.timeInMillis)
                    onDismiss()
                },
                onClearAll = {
                    onClearAll()
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun DateFilterCard(
    years: List<Int>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onApply: () -> Unit,
    onClearAll: () -> Unit,
) {
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val hardShadow = WildexTheme.extraColors.cartridgeHardShadow
    val cardSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBodySurface = MaterialTheme.colorScheme.surfaceVariant
    val missionBg = WildexColorRoles.missionCtaBackground()
    val missionFg = WildexColorRoles.missionCtaForeground()

    fun maxDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val days = (1..maxDay(selectedYear, selectedMonth)).toList()

    Box(
        modifier = Modifier
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
            Column(modifier = Modifier.fillMaxWidth()) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(missionBg, RectangleShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = missionFg,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "날짜 필터".uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = missionFg,
                    )
                }
                // 구분선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(WildexDimens.borderStrokeChunky)
                        .background(outline, RectangleShape),
                )
                // 본문: 년/월/일 드롭다운
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBodySurface, RectangleShape)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WildexDropdown(
                        items = years,
                        selected = selectedYear,
                        onSelect = onYearChange,
                        label = { "${it}년" },
                        modifier = Modifier.weight(2f),
                    )
                    WildexDropdown(
                        items = (1..12).toList(),
                        selected = selectedMonth,
                        onSelect = onMonthChange,
                        label = { "${it}월" },
                        modifier = Modifier.weight(1.2f),
                    )
                    WildexDropdown(
                        items = days,
                        selected = selectedDay.coerceAtMost(days.last()),
                        onSelect = onDayChange,
                        label = { "${it}일" },
                        modifier = Modifier.weight(1.2f),
                    )
                }
                // 액션 버튼
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBodySurface, RectangleShape)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DateFilterActionButton(
                        text = "적용",
                        onClick = onApply,
                        containerColor = missionBg,
                        contentColor = missionFg,
                    )
                    DateFilterActionButton(
                        text = "전체",
                        onClick = onClearAll,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            // 코너 블록 (좌상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset((-WildexDimens.gridStep), (-WildexDimens.gridStep))
                    .size(12.dp)
                    .zIndex(1f)
                    .background(outline, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(4.dp).background(cardSurface, RectangleShape))
            }
            // 코너 블록 (우하단)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(WildexDimens.gridStep, WildexDimens.gridStep)
                    .size(12.dp)
                    .zIndex(1f)
                    .background(outline, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(4.dp).background(cardSurface, RectangleShape))
            }
        }
    }
}

@Composable
private fun DateFilterActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    val debouncedOnClick = rememberDebounceClick(onClick)
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val hardShadow = WildexTheme.extraColors.cartridgeHardShadow
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowOffset = if (isPressed) 0.dp else depth
    val contentInset = if (isPressed) depth else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = depth, bottom = depth),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(shadowOffset, shadowOffset)
                    .background(hardShadow, RectangleShape),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(contentInset, contentInset)
                    .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                    .background(containerColor, RectangleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = debouncedOnClick,
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
