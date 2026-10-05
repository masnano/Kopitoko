package com.android.kopitoko.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.domain.model.PromoCaption
import com.android.kopitoko.domain.model.ShippingInfo
import com.android.kopitoko.ui.theme.KopitokoTheme
import com.android.kopitoko.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareHubBottomSheet(
    invoice: Invoice? = null,
    caption: PromoCaption? = null,
    imageBitmap: Bitmap? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Hub",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Quick Copy & Share Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1-click sharing to WhatsApp, Instagram, Telegram & Clipboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Share via WhatsApp
                item {
                    ShareOptionCard(
                        title = "Share via WhatsApp",
                        subtitle = if (invoice != null) "Send Invoice to customer" else "Share promo post",
                        icon = Icons.AutoMirrored.Rounded.Message,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            val text = when {
                                invoice != null -> ShareUtil.formatInvoiceText(invoice)
                                caption != null -> ShareUtil.formatPromoCaptionText(caption)
                                else -> "Halo! Silakan cek layanan usaha kami di Kopitoko Copilot."
                            }
                            ShareUtil.shareToWhatsApp(context, text, imageBitmap)
                            onDismiss()
                        }
                    )
                }

                // Copy Shipping Label
                if (invoice != null) {
                    item {
                        ShareOptionCard(
                            title = "Copy Shipping Label",
                            subtitle = "Expedition resi card",
                            icon = Icons.Rounded.LocalShipping,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = {
                                ShareUtil.copyShippingLabel(context, invoice)
                                onDismiss()
                            }
                        )
                    }
                }

                // Copy Invoice Text
                if (invoice != null) {
                    item {
                        ShareOptionCard(
                            title = "Copy Invoice Text",
                            subtitle = "Full receipt details",
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = {
                                ShareUtil.copyInvoiceText(context, invoice)
                                onDismiss()
                            }
                        )
                    }
                }

                // Copy QRIS Link / Code
                if (invoice != null) {
                    item {
                        ShareOptionCard(
                            title = "Copy QRIS Payment",
                            subtitle = "Transfer & QRIS summary",
                            icon = Icons.Rounded.QrCode,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = {
                                ShareUtil.copyQrisPaymentSummary(context, invoice)
                                onDismiss()
                            }
                        )
                    }
                }

                // Share Caption + Photo
                if (caption != null || imageBitmap != null) {
                    item {
                        ShareOptionCard(
                            title = "Share Caption + Photo",
                            subtitle = "Post to Instagram / Gallery",
                            icon = Icons.Rounded.PhotoCamera,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = {
                                val text = caption?.let { ShareUtil.formatPromoCaptionText(it) } ?: ""
                                if (imageBitmap != null) {
                                    ShareUtil.shareImageWithCaption(context, imageBitmap, text)
                                } else {
                                    ShareUtil.shareToInstagram(context, text)
                                }
                                onDismiss()
                            }
                        )
                    }
                }

                // Share via Telegram
                item {
                    ShareOptionCard(
                        title = "Share via Telegram",
                        subtitle = "Send text / media",
                        icon = Icons.AutoMirrored.Rounded.Send,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        onClick = {
                            val text = when {
                                invoice != null -> ShareUtil.formatInvoiceText(invoice)
                                caption != null -> ShareUtil.formatPromoCaptionText(caption)
                                else -> "Halo dari Kopitoko Copilot!"
                            }
                            ShareUtil.shareToTelegram(context, text, imageBitmap)
                            onDismiss()
                        }
                    )
                }

                // Send via SMS
                if (invoice != null) {
                    item {
                        ShareOptionCard(
                            title = "Send via SMS",
                            subtitle = "Direct text message",
                            icon = Icons.AutoMirrored.Rounded.Message,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = {
                                val text = ShareUtil.formatInvoiceText(invoice)
                                ShareUtil.shareToSms(context, text)
                                onDismiss()
                            }
                        )
                    }
                }

                // General Share Sheet
                item {
                    ShareOptionCard(
                        title = "General Share",
                        subtitle = "Choose external app",
                        icon = Icons.Rounded.ContentCopy,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        onClick = {
                            val text = when {
                                invoice != null -> ShareUtil.formatInvoiceText(invoice)
                                caption != null -> ShareUtil.formatPromoCaptionText(caption)
                                else -> "Kopitoko Micro-Merchant Copilot"
                            }
                            if (imageBitmap != null) {
                                ShareUtil.shareImageWithCaption(context, imageBitmap, text)
                            } else {
                                ShareUtil.shareText(context, text)
                            }
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ShareOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmbeddedQuickShareHubCard(
    invoice: Invoice? = null,
    caption: PromoCaption? = null,
    imageBitmap: Bitmap? = null,
    onOpenFullHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Quick Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1-Click Quick Share Hub",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenFullHub() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    onClick = {
                        val text = when {
                            invoice != null -> ShareUtil.formatInvoiceText(invoice)
                            caption != null -> ShareUtil.formatPromoCaptionText(caption)
                            else -> "Kopitoko Copilot"
                        }
                        ShareUtil.shareToWhatsApp(context, text, imageBitmap)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Message,
                            contentDescription = "WhatsApp",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (invoice != null) {
                    OutlinedCard(
                        onClick = { ShareUtil.copyShippingLabel(context, invoice) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalShipping,
                                contentDescription = "Label",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Shipping",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedCard(
                        onClick = { ShareUtil.copyQrisPaymentSummary(context, invoice) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCode,
                                contentDescription = "QRIS",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "QRIS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (caption != null || imageBitmap != null) {
                    OutlinedCard(
                        onClick = {
                            val text = caption?.let { ShareUtil.formatPromoCaptionText(it) } ?: ""
                            ShareUtil.copyPromoCaption(
                                context,
                                caption ?: PromoCaption(
                                    id = "temp",
                                    captionText = text
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy Text",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmbeddedQuickShareHubCardPreview() {
    KopitokoTheme {
        EmbeddedQuickShareHubCard(
            invoice = Invoice(
                id = "1",
                invoiceNumber = "INV-2026-001",
                customerName = "Budi Santoso",
                totalAmount = 50000.0,
                paymentStatus = PaymentStatus.PAID,
                shippingInfo = ShippingInfo(
                    recipientName = "Budi Santoso",
                    phone = "081234567890",
                    address = "Jl. Sudirman No 10"
                )
            ),
            onOpenFullHub = {}
        )
    }
}
