package dev.comon.wildex.capture

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.comon.wildex.data.AnalysisResultStore
import dev.comon.wildex.domain.model.BirdPrediction
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexPalette
import dev.comon.wildex.ui.theme.WildexTheme

private val rankLabels = listOf("#1", "#2", "#3")

@Composable
fun CaptureResultScreen(
    speciesId: String,
    modifier: Modifier = Modifier,
) {
    val predictions = remember { AnalysisResultStore.latestPredictions }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = WildexDimens.gridMajor, vertical = WildexDimens.gridMajor),
        verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
    ) {
        ResultSectionLabel()

        if (predictions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "결과 없음",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            predictions.forEachIndexed { index, prediction ->
                PredictionCard(
                    rank = rankLabels.getOrElse(index) { "#${index + 1}" },
                    prediction = prediction,
                    isTop = index == 0,
                )
            }
        }
    }
}

@Composable
private fun ResultSectionLabel() {
    val depth = WildexDimens.shadowOffsetHard
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
        )
        Box(
            modifier = Modifier
                .border(
                    width = WildexDimens.borderStrokeChunky,
                    color = WildexTheme.extraColors.cartridgeOutline,
                    shape = RectangleShape,
                )
                .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = "ANALYSIS COMPLETE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = WildexColorRoles.missionCtaForeground(),
            )
        }
    }
}

@Composable
private fun PredictionCard(
    rank: String,
    prediction: BirdPrediction,
    isTop: Boolean,
    modifier: Modifier = Modifier,
) {
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val shadow = WildexTheme.extraColors.cartridgeHardShadow
    val accent = WildexPalette.SpecSheetPureRed

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = depth, bottom = depth),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(shadow, RectangleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RectangleShape)
                .padding(WildexDimens.gridMajor),
            verticalArrangement = Arrangement.spacedBy(WildexDimens.gridStep),
        ) {
            // 순위 + 지시자 행
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep),
            ) {
                if (isTop) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent, CircleShape),
                    )
                }
                Text(
                    text = rank,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = if (isTop) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "RESULT",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = WildexDimens.borderStrokeChunky,
                color = outline.copy(alpha = 0.35f),
            )

            // 학명
            Text(
                text = prediction.scientificName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // 일반명 (있을 때만)
            if (!prediction.commonName.isNullOrBlank()) {
                Text(
                    text = prediction.commonName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(WildexDimens.gridStep))

            // 점수 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
            ) {
                ScoreChip(label = "SCORE", value = prediction.combinedScore)
                ScoreChip(label = "VISION", value = prediction.visionScore)
            }
        }
    }
}

@Composable
private fun ScoreChip(
    label: String,
    value: Double,
) {
    val outline = WildexTheme.extraColors.cartridgeOutline
    Row(
        modifier = Modifier
            .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
            .padding(horizontal = WildexDimens.gridStep * 2, vertical = WildexDimens.gridStep),
        horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "%.1f%%".format(value * 100),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
