package dev.comon.wildex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.comon.wildex.component.WildexMenuButton
import dev.comon.wildex.component.WildexMenuButtonStyle
import dev.comon.wildex.ui.theme.WildexTheme
import kotlinx.coroutines.launch

/**
 * 메인 메뉴와 동일한 방식으로 [WildexMenuButton]을 배치한 테스트 화면.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonTestScreen(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "메뉴 버튼 테스트",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WildexMenuButton(
                titleText = "Capture",
                subtitleText = "Scan new specimen",
                imageVector = Icons.Filled.CameraAlt,
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Capture 탭")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = WildexMenuButtonStyle.Primary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WildexMenuButton(
                    titleText = "Collection",
                    subtitleText = "Findings",
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Collection 탭")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    style = WildexMenuButtonStyle.Secondary,
                )
                WildexMenuButton(
                    titleText = "Settings",
                    subtitleText = "Configure",
                    imageVector = Icons.Filled.Settings,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Settings 탭")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    style = WildexMenuButtonStyle.Secondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ButtonTestScreenPreview() {
    WildexTheme {
        ButtonTestScreen()
    }
}
