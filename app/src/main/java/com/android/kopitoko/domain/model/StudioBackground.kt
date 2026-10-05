package com.android.kopitoko.domain.model

import androidx.compose.ui.graphics.Color

enum class StudioBackgroundType {
    CLEAN_STUDIO_WHITE,
    WOOD_TABLE_AESTHETIC,
    SOFT_PASTEL_PINK,
    MINIMALIST_MARBLE,
    WARM_COFFEE_SHOP
}

data class StudioBackgroundTheme(
    val type: StudioBackgroundType,
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    companion object {
        val ALL_THEMES = listOf(
            StudioBackgroundTheme(
                type = StudioBackgroundType.CLEAN_STUDIO_WHITE,
                title = "Clean Studio White",
                description = "Minimalist studio setup with clean bright lighting",
                primaryColor = Color(0xFFF8F9FA),
                secondaryColor = Color(0xFFE9ECEF),
                accentColor = Color(0xFF212529)
            ),
            StudioBackgroundTheme(
                type = StudioBackgroundType.WOOD_TABLE_AESTHETIC,
                title = "Wood Table Aesthetic",
                description = "Warm wooden tabletop with rustic natural light",
                primaryColor = Color(0xFF3E2723),
                secondaryColor = Color(0xFF5D4037),
                accentColor = Color(0xFFFFD54F)
            ),
            StudioBackgroundTheme(
                type = StudioBackgroundType.SOFT_PASTEL_PINK,
                title = "Soft Pastel Pink",
                description = "Trendy aesthetic pastel background with soft glow",
                primaryColor = Color(0xFFFCE4EC),
                secondaryColor = Color(0xFFF8BBD0),
                accentColor = Color(0xFFAD1457)
            ),
            StudioBackgroundTheme(
                type = StudioBackgroundType.MINIMALIST_MARBLE,
                title = "Minimalist Marble",
                description = "Elegant white marble texture with gold accents",
                primaryColor = Color(0xFFECEFF1),
                secondaryColor = Color(0xFFCFD8DC),
                accentColor = Color(0xFFFFB300)
            ),
            StudioBackgroundTheme(
                type = StudioBackgroundType.WARM_COFFEE_SHOP,
                title = "Warm Coffee Shop",
                description = "Cozy cafe ambiance with warm amber lighting",
                primaryColor = Color(0xFF2E1C14),
                secondaryColor = Color(0xFF4E342E),
                accentColor = Color(0xFFFFB74D)
            )
        )
    }
}
