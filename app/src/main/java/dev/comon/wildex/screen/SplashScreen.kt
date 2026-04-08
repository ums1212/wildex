package dev.comon.wildex.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.comon.wildex.data.LocalThemePreferencesRepository
import dev.comon.wildex.data.ThemePreferencesRepository
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(themeRepo: ThemePreferencesRepository) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepo.darkThemeOverride.first()
            _isReady.value = true
        }
    }
}

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeRepo = LocalThemePreferencesRepository.current
    val splashViewModel: SplashViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SplashViewModel(themeRepo) }
        },
    )
    val isReady by splashViewModel.isReady.collectAsStateWithLifecycle()

    LaunchedEffect(isReady) {
        if (isReady) onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
        ) {
            SplashTitle()
            SplashLoadingDots()
        }
    }
}

@Composable
private fun SplashTitle() {
    val hard = WildexTheme.extraColors.cartridgeHardShadow
    val lift = WildexDimens.shadowOffsetHard

    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "WILDEX",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = hard,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(lift, lift),
        )
        Text(
            text = "WILDEX",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SplashLoadingDots() {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            tick++
        }
    }
    val activeIndex = tick % 3
    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.surfaceContainerHighest
    val outline = WildexTheme.extraColors.cartridgeOutline

    Row(
        horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep * 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            Box(
                Modifier
                    .size(WildexDimens.gridStep * 3)
                    .border(WildexDimens.gridStep / 4, outline, RectangleShape)
                    .background(if (i == activeIndex) active else idle),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SplashScreenPreview() {
    WildexTheme {
        SplashScreen(onFinished = {})
    }
}
