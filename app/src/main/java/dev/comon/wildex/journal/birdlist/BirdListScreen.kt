package dev.comon.wildex.journal.birdlist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.comon.wildex.component.WildexClickableCard
import dev.comon.wildex.domain.model.BirdSummary
import dev.comon.wildex.ui.theme.WildexColorRoles
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme

@Composable
fun BirdListScreen(
    onBirdClick: (speciesId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BirdListViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BirdListUiEvent.NavigateToBirdInfo -> onBirdClick(event.speciesId)
                is BirdListUiEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = WildexColorRoles.missionCtaBackground()
                    )
                }
            }
            state.error != null && state.items.isEmpty() -> {
                BirdListErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.onIntent(BirdListIntent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                BirdListContent(
                    state = state,
                    onBirdClick = { viewModel.onIntent(BirdListIntent.BirdClicked(it)) },
                    onLoadMore = { viewModel.onIntent(BirdListIntent.LoadMore) },
                )
            }
        }
    }
}

@Composable
private fun BirdListContent(
    state: BirdListUiState,
    onBirdClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = WildexDimens.gridMajor,
            vertical = WildexDimens.gridMajor,
        ),
        verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
    ) {
        itemsIndexed(state.items, key = { _, bird -> bird.speciesId }) { _, bird ->
            BirdListCard(bird = bird, onClick = { onBirdClick(bird.speciesId) })
        }
        if (state.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = WildexColorRoles.missionCtaBackground(),
                    )
                }
            }
        }
    }
}

@Composable
fun BirdListCard(
    bird: BirdSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WildexClickableCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        // 종 번호 박스 — 카트리지 탭 스타일
        Box(
            modifier = Modifier
                .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RectangleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = bird.speciesId.takeLast(4).padStart(4, '0'),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
                color = WildexColorRoles.missionCtaBackground(),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = bird.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = bird.scientificName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // 분류 태그
        if (bird.familyName.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text(
                    text = bird.familyName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = WildexColorRoles.missionCtaForeground(),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun BirdListErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val depth = WildexDimens.shadowOffsetHard
    Column(
        modifier = modifier.padding(WildexDimens.gridMajor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "ERROR",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = WildexColorRoles.missionCtaBackground(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(WildexDimens.gridMajor))
        Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(depth, depth)
                    .background(WildexTheme.extraColors.cartridgeHardShadow, RectangleShape),
            )
            Box(
                modifier = Modifier
                    .border(WildexDimens.borderStrokeChunky, WildexTheme.extraColors.cartridgeOutline, RectangleShape)
                    .background(WildexColorRoles.missionCtaBackground(), RectangleShape)
                    .clickable(role = Role.Button, onClick = onRetry)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "RETRY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = WildexColorRoles.missionCtaForeground(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BirdListCardPreview() {
    WildexTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BirdListCard(
                bird = BirdSummary(
                    speciesId = "NNABR0000001",
                    name = "황조롱이",
                    scientificName = "Falco tinnunculus",
                    familyName = "매과",
                    copyright = "",
                ),
                onClick = {},
            )
            BirdListCard(
                bird = BirdSummary(
                    speciesId = "NNABR0000002",
                    name = "독수리",
                    scientificName = "Aegypius monachus",
                    familyName = "수리과",
                    copyright = "",
                ),
                onClick = {},
            )
        }
    }
}
