package com.android.kopitoko.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.ui.architecture.UiState
import com.android.kopitoko.ui.components.ClipboardOrderBanner
import com.android.kopitoko.ui.components.EditInvoiceDialog
import com.android.kopitoko.ui.components.ParseChatDialog
import com.android.kopitoko.ui.components.ShareHubBottomSheet
import com.android.kopitoko.ui.theme.KopitokoTheme
import com.android.kopitoko.ui.viewmodel.InvoiceViewModel

@Composable
fun InvoicesScreen(
    invoiceViewModel: InvoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by invoiceViewModel.invoiceListState.collectAsState()
    val detectedClipboardText by invoiceViewModel.detectedClipboardText.collectAsState()
    val chatParsingState by invoiceViewModel.chatParsingState.collectAsState()
    val editingInvoice by invoiceViewModel.editingInvoice.collectAsState()
    val showParseDialog by invoiceViewModel.showParseDialog.collectAsState()

    var selectedShareInvoice by remember { mutableStateOf<Invoice?>(null) }

    LaunchedEffect(Unit) {
        invoiceViewModel.checkClipboard(context)
    }

    if (showParseDialog) {
        ParseChatDialog(
            initialText = detectedClipboardText ?: "",
            parsingState = chatParsingState,
            onParseText = { invoiceViewModel.parseChatToInvoice(it) },
            onDismiss = { invoiceViewModel.closeParseDialog() }
        )
    }

    editingInvoice?.let { inv ->
        EditInvoiceDialog(
            invoice = inv,
            onUpdateInvoice = { invoiceViewModel.updateEditingInvoice(it) },
            onSaveInvoice = { invoiceViewModel.saveEditingInvoice() },
            onCancel = { invoiceViewModel.cancelEditingInvoice() }
        )
    }

    selectedShareInvoice?.let { shareInv ->
        ShareHubBottomSheet(
            invoice = shareInv,
            onDismiss = { selectedShareInvoice = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { invoiceViewModel.openParseDialog(detectedClipboardText) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                icon = { Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null) },
                text = { Text("Smart Chat Invoice") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val uiState = state) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading invoices...")
                }
            }
            is UiState.Success -> {
                InvoiceListContent(
                    invoices = uiState.data,
                    detectedClipboardText = detectedClipboardText,
                    onOpenParseDialog = { invoiceViewModel.openParseDialog(it) },
                    onDismissClipboardBanner = { invoiceViewModel.dismissClipboardBanner() },
                    onInvoiceClick = { invoiceViewModel.updateEditingInvoice(it) },
                    onShareInvoice = { selectedShareInvoice = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            UiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No invoices created yet.")
                }
            }
        }
    }
}

@Composable
fun InvoiceListContent(
    invoices: List<Invoice>,
    detectedClipboardText: String?,
    onOpenParseDialog: (String?) -> Unit,
    onDismissClipboardBanner: () -> Unit,
    onInvoiceClick: (Invoice) -> Unit,
    onShareInvoice: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Invoices & Receipts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${invoices.size} Active Transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (detectedClipboardText != null) {
            item {
                ClipboardOrderBanner(
                    copiedText = detectedClipboardText,
                    onParseClick = { onOpenParseDialog(detectedClipboardText) },
                    onDismissClick = onDismissClipboardBanner
                )
            }
        }

        item {
            OutlinedCard(
                onClick = { onOpenParseDialog(detectedClipboardText) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentPaste,
                        contentDescription = "Parse Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Parse Copied Chat to Invoice",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paste customer WhatsApp order to extract fields",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items(invoices, key = { it.id }) { invoice ->
            InvoiceItemCard(
                invoice = invoice,
                onClick = { onInvoiceClick(invoice) },
                onShareClick = { onShareInvoice(invoice) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun InvoiceItemCard(
    invoice: Invoice,
    onClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = invoice.paymentStatus.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.paymentStatus == PaymentStatus.PAID)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = "Customer: ${invoice.customerName} ${invoice.customerPhone?.let { "($it)" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (invoice.shippingInfo?.address?.isNotBlank() == true) {
                    Text(
                        text = "Address: ${invoice.shippingInfo.address} • Courier: ${invoice.shippingInfo.courier}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${invoice.items.size} Items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: Rp ${invoice.totalAmount.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Quick Share",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceItemCardPreview() {
    KopitokoTheme {
        InvoiceItemCard(
            invoice = Invoice(
                id = "1",
                invoiceNumber = "INV-2026-001",
                customerName = "Siti Rahma",
                totalAmount = 36000.0,
                paymentStatus = PaymentStatus.PAID
            ),
            onClick = {},
            onShareClick = {}
        )
    }
}
