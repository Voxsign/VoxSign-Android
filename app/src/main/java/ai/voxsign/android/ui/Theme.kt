package ai.voxsign.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import java.util.Locale

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    background = Color(0xFFF7F7F8),
    surface = Color(0xFFFFFFFF)
)

/**
 * Scale line heights by 1.2x for Arabic script per DESIGN.md §9.2
 * ("Line height +20% for Arabic script"). Western digits are kept by default.
 */
private fun scaledTypography(scaleLineHeight: Float): Typography {
    val base = Typography()
    fun TextStyle.scaled() = copy(lineHeight = (lineHeight.value * scaleLineHeight).sp)
    return Typography(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled(),
    )
}

@Composable
fun VoxSignTheme(content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val locale: Locale = config.locales[0]
    val isArabic = locale.language == "ar"
    val typography = if (isArabic) scaledTypography(1.2f) else Typography()
    MaterialTheme(colorScheme = LightColors, typography = typography, content = content)
}
