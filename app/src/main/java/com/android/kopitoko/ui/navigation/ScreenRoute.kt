package com.android.kopitoko.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface ScreenRoute {
    val routeTitle: String
    val icon: ImageVector

    @Serializable
    data object Home : ScreenRoute {
        override val routeTitle: String = "Home"
        override val icon: ImageVector = Icons.Rounded.Home
    }

    @Serializable
    data object Products : ScreenRoute {
        override val routeTitle: String = "Products"
        override val icon: ImageVector = Icons.Rounded.Inventory2
    }

    @Serializable
    data object Invoices : ScreenRoute {
        override val routeTitle: String = "Invoices"
        override val icon: ImageVector = Icons.AutoMirrored.Rounded.ReceiptLong
    }

    @Serializable
    data object Captions : ScreenRoute {
        override val routeTitle: String = "AI Captions"
        override val icon: ImageVector = Icons.Rounded.AutoAwesome
    }

    @Serializable
    data object ProductEnhancer : ScreenRoute {
        override val routeTitle: String = "Photo Studio"
        override val icon: ImageVector = Icons.Rounded.PhotoCamera
    }

    @Serializable
    data object CameraCapture : ScreenRoute {
        override val routeTitle: String = "Camera"
        override val icon: ImageVector = Icons.Rounded.CameraAlt
    }

    @Serializable
    data object Settings : ScreenRoute {
        override val routeTitle: String = "Settings"
        override val icon: ImageVector = Icons.Rounded.Settings
    }
}
