package dev.comon.wildex.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.comon.wildex.ui.theme.WildexTheme

@Composable
fun GuideScreen(
    onBackClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun GuideScreenPreview() {
    WildexTheme {
        GuideScreen(onBackClick = {})
    }
}
