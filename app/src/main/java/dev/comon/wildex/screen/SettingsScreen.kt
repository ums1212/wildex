package dev.comon.wildex.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val repository = LocalThemePreferencesRepository.current
    val systemDark = isSystemInDarkTheme()
    val override by repository.darkThemeOverride.collectAsStateWithLifecycle(initialValue = null)
    val darkThemeActive = override ?: systemDark
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = WildexDimens.borderStrokeChunky,
                    color = WildexTheme.extraColors.cartridgeOutline,
                    shape = RectangleShape,
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RectangleShape,
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "다크 테마",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = darkThemeActive,
                    onCheckedChange = { checked ->
                        scope.launch {
                            repository.setDarkTheme(checked)
                        }
                    },
                )
            }
            Text(
                text = "켜면 어두운 화면, 끄면 밝은 화면으로 표시됩니다. 앱을 다시 켜도 유지됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val bgmEnabled by repository.bgmEnabled.collectAsStateWithLifecycle(initialValue = true)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = WildexDimens.borderStrokeChunky,
                    color = WildexTheme.extraColors.cartridgeOutline,
                    shape = RectangleShape,
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RectangleShape,
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "배경음악",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = bgmEnabled,
                    onCheckedChange = { checked ->
                        scope.launch {
                            repository.setBgmEnabled(checked)
                        }
                    },
                )
            }
            Text(
                text = "배경음악을 켜거나 끌 수 있습니다. 테마에 따라 다른 음악이 재생됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
