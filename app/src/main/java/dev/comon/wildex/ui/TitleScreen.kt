package dev.comon.wildex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.comon.wildex.ui.theme.WildexButtonDefaults
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexTheme

/**
 * 로그인 전 타이틀·로그인 진입용 플레이스홀더 화면 (실제 인증 없음).
 * [onLoginClick]은 UI 데모용으로 메인으로 복귀·로그인 상태 전환에만 사용됩니다.
 */
@Composable
fun TitleScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = WildexDimens.borderStrokeChunky,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RectangleShape,
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RectangleShape,
                )
                .padding(horizontal = 20.dp, vertical = 28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "WILDEX",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "계정으로 로그인하면 기록이 동기화됩니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.size(32.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            border = WildexButtonDefaults.chunkyOutlineBorder(),
            colors = WildexButtonDefaults.primaryColors(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        ) {
            Text(
                text = "로그인",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun TitleScreenPreview() {
    WildexTheme {
        TitleScreen(onLoginClick = { })
    }
}
