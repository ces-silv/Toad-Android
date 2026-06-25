package org.ckdk.toad_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.ckdk.toad_app.R

// POPPINS — Titles, headings, and buttons
val PoppinsFontFamily = FontFamily(
    Font(resId = R.font.poppins_regular,   weight = FontWeight.Normal),
    Font(resId = R.font.poppins_medium,    weight = FontWeight.Medium),
    Font(resId = R.font.poppins_semibold,  weight = FontWeight.SemiBold),
    Font(resId = R.font.poppins_bold,      weight = FontWeight.Bold),
)

// INTER — Body Text y Tags
val InterFontFamily = FontFamily(
    Font(resId = R.font.inter_variablefont_opsz_wght, weight = FontWeight.Normal,   style = FontStyle.Normal),
    Font(resId = R.font.inter_variablefont_opsz_wght,          weight = FontWeight.Medium,   style = FontStyle.Normal),
    Font(resId = R.font.inter_variablefont_opsz_wght,          weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(resId = R.font.inter_italic_variablefont_opsz_wght,   weight = FontWeight.Normal,   style = FontStyle.Italic),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)