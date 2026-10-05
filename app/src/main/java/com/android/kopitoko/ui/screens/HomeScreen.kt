package com.android.kopitoko.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.ui.architecture.UiState
import com.android.kopitoko.ui.components.ClipboardOrderBanner
import com.android.kopitoko.ui.components.EmbeddedQuickShareHubCard
import com.android.kopitoko.ui.components.ShareHubBottomSheet
import com.android.kopitoko.ui.theme.KopitokoTheme
import com.android.kopitoko.ui.viewmodel.InvoiceViewModel

@Composable
fun HomeScreen(
    isApiKeyConfigured: Boolean,
    onNavigateToCaptions: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    invoiceViewModel: InvoiceViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detectedClipboardText = invoiceViewModel?.detectedClipboardText?.collectAsState()?.value
    val invoiceListState = invoiceViewModel?.invoiceListState?.collectAsState()?.value
    val latestInvoice: Invoice? = (invoiceListState as? UiState.Success)?.data?.firstOrNull()

    var showShareHub by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        invoiceViewModel?.checkClipboard(context)
    }

    if (showShareHub) {
        ShareHubBottomSheet(
            invoice = latestInvoice,
            onDismiss = { showShareHub = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Kopitoko Micro-Merchant Copilot",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Empowering your local business with automated receipts, AI promo captions, and smart inventory.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (detectedClipboardText != null) {
            item {
                ClipboardOrderBanner(
                    copiedText = detectedClipboardText,
                    onParseClick = {
                        invoiceViewModel.openParseDialog(detectedClipboardText)
                        onNavigateToInvoices()
                    },
                    onDismissClick = {
                        invoiceViewModel.dismissClipboardBanner()
                    }
                )
            }
        }

        if (!isApiKeyConfigured) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "Key Warning",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gemini API Key Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Configure your API key in Settings to activate AI features.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        OutlinedButton(onClick = onNavigateToSettings) {
                            Text("Configure")
                        }
                    }
                }
            }
        }

        item {
            EmbeddedQuickShareHubCard(
                invoice = latestInvoice,
                onOpenFullHub = { showShareHub = true }
            )
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Smart Chat Invoice",
                    subtitle = "Parse WhatsApp order",
                    icon = Icons.Rounded.ContentPaste,
                    onClick = {
                        invoiceViewModel?.openParseDialog(detectedClipboardText)
                        onNavigateToInvoices()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "AI Captions",
                    subtitle = "Generate social posts",
                    icon = Icons.Rounded.AutoAwesome,
                    onClick = onNavigateToCaptions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Invoices",
                    subtitle = "Scan or create receipts",
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    onClick = onNavigateToInvoices,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Products",
                    subtitle = "Manage inventory",
                    icon = Icons.Rounded.Inventory2,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KopitokoTheme {
        HomeScreen(
            isApiKeyConfigured = false,
            onNavigateToCaptions = {},
            onNavigateToInvoices = {},
            onNavigateToProducts = {},
            onNavigateToSettings = {}
        )
    }
}
