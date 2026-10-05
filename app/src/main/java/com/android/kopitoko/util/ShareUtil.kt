package com.android.kopitoko.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.PromoCaption
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object ShareUtil {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    /**
     * Formats an Invoice object into clean, formatted text for sharing via WhatsApp, SMS, or plain text.
     */
    fun formatInvoiceText(invoice: Invoice): String {
        val formattedTotal = rupiahFormat.format(invoice.totalAmount)
        val formattedSubtotal = rupiahFormat.format(invoice.subtotal)
        val formattedDiscount = rupiahFormat.format(invoice.discount)
        val shippingFee = invoice.shippingInfo?.shippingFee ?: 0.0
        val formattedShippingFee = rupiahFormat.format(shippingFee)

        val itemsList = if (invoice.items.isNotEmpty()) {
            invoice.items.joinToString("\n") { item ->
                "• ${item.productName} (${item.quantity}x) - ${rupiahFormat.format(item.totalPrice)}"
            }
        } else {
            "• - "
        }

        val statusText = when (invoice.paymentStatus.name) {
            "PAID" -> "LUNAS ✅"
            "UNPAID" -> "BELUM DIBAYAR ⏳"
            "PARTIALLY_PAID" -> "SEBAGIAN ⌛"
            "REFUNDED" -> "REFUND ↩️"
            else -> invoice.paymentStatus.name
        }

        val notesSection = if (!invoice.notes.isNullOrEmpty()) "Catatan   : ${invoice.notes}\n" else ""

        return """
            Receipt / Invoice: ${invoice.invoiceNumber}
            ----------------------------------------
            Pelanggan : ${invoice.customerName}
            No HP     : ${invoice.customerPhone ?: "-"}
            Status    : $statusText
            ----------------------------------------
            RINCIAN PESANAN:
            $itemsList
            ----------------------------------------
            Subtotal  : $formattedSubtotal
            Diskon    : -$formattedDiscount
            Ongkir    : $formattedShippingFee
            TOTAL     : $formattedTotal
            ----------------------------------------
            Metode Pembayaran : ${invoice.paymentMethod ?: "QRIS / Transfer Bank"}
            Kurir / Expedisi  : ${invoice.shippingInfo?.courier ?: "GoSend / GrabExpress"}
            Alamat Pengiriman : ${invoice.shippingInfo?.address ?: "-"}
            $notesSection----------------------------------------
            Terima kasih telah berbelanja di Kopitoko!
        """.trimIndent()
    }

    /**
     * Formats an expedition shipping label card text for quick copy or share to courier drivers.
     */
    fun formatShippingLabel(invoice: Invoice): String {
        val shipping = invoice.shippingInfo
        val recipient = shipping?.recipientName?.ifBlank { invoice.customerName } ?: invoice.customerName
        val phone = shipping?.phone?.ifBlank { invoice.customerPhone ?: "-" } ?: (invoice.customerPhone ?: "-")
        val address = shipping?.address?.ifBlank { "Alamat belum diisi" } ?: "Alamat belum diisi"
        val courier = shipping?.courier?.ifBlank { "GoSend / Courier" } ?: "GoSend / Courier"
        val tracking = shipping?.trackingNumber ?: "Belum ada resi"

        val notesSection = if (!invoice.notes.isNullOrEmpty()) "Catatan  : ${invoice.notes}\n" else ""

        return """
            LABEL PENGIRIMAN EXPEDITION
            ========================================
            Penerima : $recipient
            No. Telp : $phone
            Alamat   : $address
            Kurir    : $courier
            No. Resi : $tracking
            Invoice  : ${invoice.invoiceNumber}
            ${notesSection}========================================
            [Kopitoko Shipping Label]
        """.trimIndent()
    }

    /**
     * Generates dynamic QRIS payment summary text with merchant info, amount, and payment instructions.
     */
    fun formatQrisPaymentSummary(
        invoice: Invoice,
        merchantNmid: String = "ID10202688899"
    ): String {
        val formattedTotal = rupiahFormat.format(invoice.totalAmount)
        return """
            PEMBAYARAN QRIS / TRANSFER BANK
            ----------------------------------------
            Tagihan Invoice : ${invoice.invoiceNumber}
            Nama Pelanggan  : ${invoice.customerName}
            TOTAL BAYAR     : $formattedTotal
            ----------------------------------------
            Merchant QRIS   : Kopitoko Copilot
            NMID            : $merchantNmid
            
            Transfer Bank:
            • BCA: 8830-192-888 a.n. Kopitoko
            • Mandiri: 127-00-9988776-1 a.n. Kopitoko
            
            Silakan scan kode QRIS atau transfer ke rekening di atas, lalu kirimkan bukti transfer. Terima kasih!
        """.trimIndent()
    }

    /**
     * Formats PromoCaption into shareable social media text.
     */
    fun formatPromoCaptionText(caption: PromoCaption): String {
        val hashtagsStr = if (caption.hashtags.isNotEmpty()) {
            "\n\n" + caption.hashtags.joinToString(" ")
        } else ""

        val ctaStr = if (!caption.callToAction.isNullOrBlank()) {
            "\n\n👉 " + caption.callToAction
        } else ""

        return caption.captionText + ctaStr + hashtagsStr
    }

    /**
     * Copies formatted invoice text to clipboard.
     */
    fun copyInvoiceText(context: Context, invoice: Invoice): Boolean {
        return ClipboardUtil.copyToClipboard(
            context = context,
            label = "Invoice ${invoice.invoiceNumber}",
            text = formatInvoiceText(invoice)
        )
    }

    /**
     * Copies formatted shipping label text to clipboard.
     */
    fun copyShippingLabel(context: Context, invoice: Invoice): Boolean {
        return ClipboardUtil.copyToClipboard(
            context = context,
            label = "Shipping Label ${invoice.invoiceNumber}",
            text = formatShippingLabel(invoice)
        )
    }

    /**
     * Copies dynamic QRIS payment summary to clipboard.
     */
    fun copyQrisPaymentSummary(
        context: Context,
        invoice: Invoice,
        merchantNmid: String = "ID10202688899"
    ): Boolean {
        return ClipboardUtil.copyToClipboard(
            context = context,
            label = "QRIS Payment Summary",
            text = formatQrisPaymentSummary(invoice, merchantNmid)
        )
    }

    /**
     * Copies promo caption text to clipboard.
     */
    fun copyPromoCaption(context: Context, caption: PromoCaption): Boolean {
        return ClipboardUtil.copyToClipboard(
            context = context,
            label = "Promo Caption",
            text = formatPromoCaptionText(caption)
        )
    }

    /**
     * Triggers Android Intent.ACTION_SEND for plain text content.
     */
    fun shareText(
        context: Context,
        text: String,
        title: String = "Share via",
        targetPackage: String? = null
    ): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (!targetPackage.isNullOrEmpty()) {
                setPackage(targetPackage)
            }
        }

        return try {
            if (!targetPackage.isNullOrEmpty()) {
                context.startActivity(sendIntent)
            } else {
                val chooserIntent = Intent.createChooser(sendIntent, title)
                context.startActivity(chooserIntent)
            }
            true
        } catch (_: ActivityNotFoundException) {
            // Target package not installed, fallback to generic chooser
            if (!targetPackage.isNullOrEmpty()) {
                try {
                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    val chooser = Intent.createChooser(fallbackIntent, title)
                    context.startActivity(chooser)
                    return true
                } catch (_: Exception) {
                    Toast.makeText(context, "No app available to handle sharing.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No app available to handle sharing.", Toast.LENGTH_SHORT).show()
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to launch share sheet.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Triggers Android Intent.ACTION_SEND for HTML formatted text.
     */
    fun shareHtmlText(
        context: Context,
        htmlContent: String,
        subject: String = "Invoice Receipt",
        title: String = "Share Receipt"
    ): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_TEXT, htmlContent)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        return try {
            val chooserIntent = Intent.createChooser(sendIntent, title)
            context.startActivity(chooserIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share HTML receipt.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Saves an image Bitmap to cacheDir and triggers Android Intent.ACTION_SEND with FileProvider URI.
     */
    fun shareImageWithCaption(
        context: Context,
        bitmap: Bitmap,
        caption: String? = null,
        targetPackage: String? = null,
        title: String = "Share Image"
    ): Boolean {
        return try {
            val cachePath = File(context.cacheDir, "shared_images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }

            val imageFile = File(cachePath, "kopitoko_shared_${System.currentTimeMillis()}.png")
            val fileOutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val authority = "${context.packageName}.fileprovider"
            val imageUri: Uri = FileProvider.getUriForFile(context, authority, imageFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                if (!caption.isNullOrEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (!targetPackage.isNullOrEmpty()) {
                    setPackage(targetPackage)
                }
            }

            if (!targetPackage.isNullOrEmpty()) {
                try {
                    context.startActivity(shareIntent)
                } catch (_: ActivityNotFoundException) {
                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        if (!caption.isNullOrEmpty()) {
                            putExtra(Intent.EXTRA_TEXT, caption)
                        }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(fallbackIntent, title))
                }
            } else {
                context.startActivity(Intent.createChooser(shareIntent, title))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Target Platform Preset Shortcuts
    fun shareToWhatsApp(context: Context, text: String, bitmap: Bitmap? = null): Boolean {
        return if (bitmap != null) {
            shareImageWithCaption(context, bitmap, caption = text, targetPackage = "com.whatsapp", title = "Share via WhatsApp")
        } else {
            shareText(context, text, title = "Share via WhatsApp", targetPackage = "com.whatsapp")
        }
    }

    fun shareToInstagram(context: Context, text: String, bitmap: Bitmap? = null): Boolean {
        return if (bitmap != null) {
            shareImageWithCaption(context, bitmap, caption = text, targetPackage = "com.instagram.android", title = "Share via Instagram")
        } else {
            shareText(context, text, title = "Share via Instagram", targetPackage = "com.instagram.android")
        }
    }

    fun shareToTelegram(context: Context, text: String, bitmap: Bitmap? = null): Boolean {
        return if (bitmap != null) {
            shareImageWithCaption(context, bitmap, caption = text, targetPackage = "org.telegram.messenger", title = "Share via Telegram")
        } else {
            shareText(context, text, title = "Share via Telegram", targetPackage = "org.telegram.messenger")
        }
    }

    fun shareToSms(context: Context, text: String): Boolean {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", text)
        }
        return try {
            context.startActivity(smsIntent)
            true
        } catch (_: Exception) {
            shareText(context, text, title = "Share via SMS")
        }
    }
}
