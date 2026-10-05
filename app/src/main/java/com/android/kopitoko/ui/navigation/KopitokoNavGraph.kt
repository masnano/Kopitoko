package com.android.kopitoko.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.android.kopitoko.ui.screens.CameraCaptureScreen
import com.android.kopitoko.ui.screens.CaptionsScreen
import com.android.kopitoko.ui.screens.HomeScreen
import com.android.kopitoko.ui.screens.InvoicesScreen
import com.android.kopitoko.ui.screens.ProductEnhancerScreen
import com.android.kopitoko.ui.screens.ProductsScreen
import com.android.kopitoko.ui.screens.SettingsScreen
import com.android.kopitoko.ui.viewmodel.ApiKeyViewModel
import com.android.kopitoko.ui.viewmodel.CaptionViewModel
import com.android.kopitoko.ui.viewmodel.InvoiceViewModel
import com.android.kopitoko.ui.viewmodel.MainViewModel
import com.android.kopitoko.ui.viewmodel.ProductViewModel

@Composable
fun KopitokoApp(
    mainViewModel: MainViewModel,
    apiKeyViewModel: ApiKeyViewModel,
    captionViewModel: CaptionViewModel,
    invoiceViewModel: InvoiceViewModel,
    productViewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    val isApiKeyConfigured by mainViewModel.isApiKeyConfigured.collectAsState()
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    var currentRoute by remember { mutableStateOf<ScreenRoute>(ScreenRoute.Home) }

    val navRoutes = listOf(
        ScreenRoute.Home,
        ScreenRoute.Products,
        ScreenRoute.Invoices,
        ScreenRoute.Captions,
        ScreenRoute.ProductEnhancer,
        ScreenRoute.Settings
    )

    if (isExpanded) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail {
                navRoutes.forEach { screen ->
                    NavigationRailItem(
                        selected = currentRoute == screen,
                        onClick = { currentRoute = screen },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.routeTitle) },
                        label = { Text(screen.routeTitle) }
                    )
                }
            }
            Scaffold(modifier = Modifier.weight(1f)) { paddingValues ->
                KopitokoScreenContent(
                    currentRoute = currentRoute,
                    isApiKeyConfigured = isApiKeyConfigured,
                    apiKeyViewModel = apiKeyViewModel,
                    captionViewModel = captionViewModel,
                    invoiceViewModel = invoiceViewModel,
                    productViewModel = productViewModel,
                    onNavigateToRoute = { currentRoute = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    navRoutes.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen,
                            onClick = { currentRoute = screen },
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.routeTitle) },
                            label = { Text(screen.routeTitle) }
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            KopitokoScreenContent(
                currentRoute = currentRoute,
                isApiKeyConfigured = isApiKeyConfigured,
                apiKeyViewModel = apiKeyViewModel,
                captionViewModel = captionViewModel,
                invoiceViewModel = invoiceViewModel,
                productViewModel = productViewModel,
                onNavigateToRoute = { currentRoute = it },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun KopitokoScreenContent(
    currentRoute: ScreenRoute,
    isApiKeyConfigured: Boolean,
    apiKeyViewModel: ApiKeyViewModel,
    captionViewModel: CaptionViewModel,
    invoiceViewModel: InvoiceViewModel,
    productViewModel: ProductViewModel,
    onNavigateToRoute: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCaptionBitmap by captionViewModel.selectedBitmap.collectAsState()
    val stagedProductBitmap by productViewModel.stagedBitmap.collectAsState()

    when (currentRoute) {
        ScreenRoute.Home -> HomeScreen(
            isApiKeyConfigured = isApiKeyConfigured,
            onNavigateToCaptions = { onNavigateToRoute(ScreenRoute.Captions) },
            onNavigateToInvoices = { onNavigateToRoute(ScreenRoute.Invoices) },
            onNavigateToProducts = { onNavigateToRoute(ScreenRoute.Products) },
            onNavigateToSettings = { onNavigateToRoute(ScreenRoute.Settings) },
            invoiceViewModel = invoiceViewModel,
            modifier = modifier
        )
        ScreenRoute.Products -> ProductsScreen(
            productViewModel = productViewModel,
            onOpenCamera = { onNavigateToRoute(ScreenRoute.CameraCapture) },
            onOpenEnhancer = { onNavigateToRoute(ScreenRoute.ProductEnhancer) },
            modifier = modifier
        )
        ScreenRoute.Invoices -> InvoicesScreen(
            invoiceViewModel = invoiceViewModel,
            modifier = modifier
        )
        ScreenRoute.Captions -> CaptionsScreen(
            captionViewModel = captionViewModel,
            onOpenCamera = { onNavigateToRoute(ScreenRoute.CameraCapture) },
            onOpenEnhancer = { onNavigateToRoute(ScreenRoute.ProductEnhancer) },
            modifier = modifier
        )
        ScreenRoute.ProductEnhancer -> ProductEnhancerScreen(
            inputBitmap = selectedCaptionBitmap ?: stagedProductBitmap,
            onOpenCamera = { onNavigateToRoute(ScreenRoute.CameraCapture) },
            onUseForCaption = { enhancedBitmap ->
                captionViewModel.setSelectedBitmap(enhancedBitmap)
                onNavigateToRoute(ScreenRoute.Captions)
            },
            onAddToProducts = { enhancedBitmap ->
                productViewModel.setStagedBitmap(enhancedBitmap)
                onNavigateToRoute(ScreenRoute.Products)
            },
            modifier = modifier
        )
        ScreenRoute.CameraCapture -> CameraCaptureScreen(
            onPhotoCaptured = { bitmap ->
                captionViewModel.setSelectedBitmap(bitmap)
                productViewModel.setStagedBitmap(bitmap)
                onNavigateToRoute(ScreenRoute.ProductEnhancer)
            },
            onBackClick = { onNavigateToRoute(ScreenRoute.Captions) },
            modifier = modifier
        )
        ScreenRoute.Settings -> SettingsScreen(
            apiKeyViewModel = apiKeyViewModel,
            modifier = modifier
        )
    }
}
