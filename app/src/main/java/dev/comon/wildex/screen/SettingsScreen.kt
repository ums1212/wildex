package dev.comon.wildex.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.comon.wildex.component.WildexClickableCard
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

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraGranted by remember { mutableStateOf(checkGranted(context, Manifest.permission.CAMERA)) }
    var fineLocationGranted by remember { mutableStateOf(checkGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) }
    var coarseLocationGranted by remember { mutableStateOf(checkGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraGranted = checkGranted(context, Manifest.permission.CAMERA)
                fineLocationGranted = checkGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                coarseLocationGranted = checkGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openAppSettings: () -> Unit = {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
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

        val cameraStatus = if (cameraGranted) "허용됨" else "거부됨"
        val locationStatus = when {
            fineLocationGranted -> "정확한 위치"
            coarseLocationGranted -> "대략적 위치"
            else -> "거부됨"
        }
        val locationGranted = fineLocationGranted || coarseLocationGranted

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
                    text = "카메라 권한",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = cameraStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (cameraGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "위치 권한",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = locationStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (locationGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "권한을 변경하려면 시스템 설정에서 직접 조정해 주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WildexClickableCard(
            onClick = openAppSettings,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "권한 설정 열기",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun checkGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
