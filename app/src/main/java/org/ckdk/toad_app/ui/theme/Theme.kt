package org.ckdk.toad_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ToadLightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = EcoWhite,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    secondary = AlertOrange,
    onSecondary = EcoWhite,
    secondaryContainer = SoftOrange,
    onSecondaryContainer = SlateGray,
    background = EcoWhite,
    onBackground = SlateGray,
    surface = EcoWhite,
    onSurface = SlateGray,
    surfaceVariant = MediumGray,
    onSurfaceVariant = TextGray,
    error = AlertOrange,
    onError = EcoWhite,
    outline = TextGray,
)

@Composable
fun Toad_AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ToadLightColorScheme,
        typography = Typography,
        content = content
    )
}