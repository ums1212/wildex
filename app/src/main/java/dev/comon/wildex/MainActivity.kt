package dev.comon.wildex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.comon.wildex.ui.DesignSystemTestScreen
import dev.comon.wildex.ui.WildexDesignSystemHost
import dev.comon.wildex.ui.theme.WildexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WildexDesignSystemHost()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DesignSystemMainPreview() {
    WildexTheme {
        DesignSystemTestScreen(darkTheme = false, onDarkThemeChange = {})
    }
}