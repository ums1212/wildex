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
    initialStartMillis: Long?,
    initialEndMillis: Long?,
    onDismiss: () -> Unit,
    onApply: (startMillis: Long, endMillis: Long) -> Unit,
    onClearAll: () -> Unit,
) {
    val now = Calendar.getInstance()
    val startCal = Calendar.getInstance().apply {
        timeInMillis = initialStartMillis ?: now.timeInMillis
    }
    val endCal = Calendar.getInstance().apply {
        timeInMillis = initialEndMillis ?: initialStartMillis ?: now.timeInMillis
    }
    val currentYear = now.get(Calendar.YEAR)
    val years = (currentYear downTo currentYear - 9).toList()

    var startYear by remember { mutableIntStateOf(startCal.get(Calendar.YEAR).coerceIn(years.last(), years.first())) }
    var startMonth by remember { mutableIntStateOf(startCal.get(Calendar.MONTH) + 1) }
    var startDay by remember { mutableIntStateOf(startCal.get(Calendar.DAY_OF_MONTH)) }

    var endYear by remember { mutableIntStateOf(endCal.get(Calendar.YEAR).coerceIn(years.last(), years.first())) }
    var endMonth by remember { mutableIntStateOf(endCal.get(Calendar.MONTH) + 1) }
    var endDay by remember { mutableIntStateOf(endCal.get(Calendar.DAY_OF_MONTH)) }

    fun maxDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun ensureEndOnOrAfterStart() {
        if (endYear < startYear ||
            (endYear == startYear && endMonth < startMonth) ||
            (endYear == startYear && endMonth == startMonth && endDay < startDay)
        ) {
            endYear = startYear
            endMonth = startMonth
            endDay = startDay
        } else if (endYear == startYear && endMonth == startMonth) {
            endDay = endDay.coerceAtLeast(startDay)
        }
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
                currentYear = currentYear,
                startYear = startYear,
                startMonth = startMonth,
                startDay = startDay,
                endYear = endYear,
                endMonth = endMonth,
                endDay = endDay,
                onStartYearChange = { y ->
                    startYear = y
                    startDay = startDay.coerceAtMost(maxDay(y, startMonth))
                    ensureEndOnOrAfterStart()
                },
                onStartMonthChange = { m ->
                    startMonth = m
                    startDay = startDay.coerceAtMost(maxDay(startYear, m))
                    ensureEndOnOrAfterStart()
                },
                onStartDayChange = { d ->
                    startDay = d
                    ensureEndOnOrAfterStart()
                },
                onEndYearChange = { y ->
                    endYear = y
                    endDay = endDay.coerceAtMost(maxDay(y, endMonth))
                },
                onEndMonthChange = { m ->
                    endMonth = m
                    endDay = endDay.coerceAtMost(maxDay(endYear, m))
                },
                onEndDayChange = { endDay = it },
                onApply = {
                    val startMillis = toEpochMillis(startYear, startMonth, startDay, endOfDay = false)
                    val endMillis = toEpochMillis(endYear, endMonth, endDay, endOfDay = true)
                    onApply(startMillis, endMillis)
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
    currentYear: Int,
    startYear: Int,
    startMonth: Int,
    startDay: Int,
    endYear: Int,
    endMonth: Int,
    endDay: Int,
    onStartYearChange: (Int) -> Unit,
    onStartMonthChange: (Int) -> Unit,
    onStartDayChange: (Int) -> Unit,
    onEndYearChange: (Int) -> Unit,
    onEndMonthChange: (Int) -> Unit,
    onEndDayChange: (Int) -> Unit,
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
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

    fun maxDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val startDays = (1..maxDay(startYear, startMonth)).toList()

    val endYears = (currentYear downTo startYear).toList()
    val endMonths = if (endYear == startYear) (startMonth..12).toList() else (1..12).toList()
    val endDays = run {
        val maxD = maxDay(endYear, endMonth)
        val minD = if (endYear == startYear && endMonth == startMonth) startDay else 1
        (minD..maxD).toList()
    }

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
                // 본문: 시작일/종료일 드롭다운
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBodySurface, RectangleShape)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 시작일
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "시작일", style = labelStyle, color = labelColor)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WildexDropdown(
                                items = years,
                                selected = startYear,
                                onSelect = onStartYearChange,
                                label = { "${it}년" },
                                modifier = Modifier.weight(2f),
                            )
                            WildexDropdown(
                                items = (1..12).toList(),
                                selected = startMonth,
                                onSelect = onStartMonthChange,
                                label = { "${it}월" },
                                modifier = Modifier.weight(1.2f),
                            )
                            WildexDropdown(
                                items = startDays,
                                selected = startDay.coerceAtMost(startDays.last()),
                                onSelect = onStartDayChange,
                                label = { "${it}일" },
                                modifier = Modifier.weight(1.2f),
                            )
                        }
                    }
                    // 종료일
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "종료일", style = labelStyle, color = labelColor)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WildexDropdown(
                                items = endYears,
                                selected = endYear,
                                onSelect = onEndYearChange,
                                label = { "${it}년" },
                                modifier = Modifier.weight(2f),
                            )
                            WildexDropdown(
                                items = endMonths,
                                selected = endMonth.coerceAtLeast(endMonths.first()),
                                onSelect = onEndMonthChange,
                                label = { "${it}월" },
                                modifier = Modifier.weight(1.2f),
                            )
                            WildexDropdown(
                                items = endDays,
                                selected = endDay.coerceIn(endDays.first(), endDays.last()),
                                onSelect = onEndDayChange,
                                label = { "${it}일" },
                                modifier = Modifier.weight(1.2f),
                            )
                        }
                    }
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

private fun toEpochMillis(year: Int, month: Int, day: Int, endOfDay: Boolean): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, day)
    if (endOfDay) {
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
    } else {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
