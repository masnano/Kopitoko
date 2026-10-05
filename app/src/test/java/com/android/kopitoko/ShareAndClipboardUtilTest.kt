package com.android.kopitoko

import com.android.kopitoko.domain.model.CaptionTone
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.OrderItem
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.domain.model.PromoCaption
import com.android.kopitoko.domain.model.ShippingInfo
import com.android.kopitoko.domain.model.SocialPlatform
import com.android.kopitoko.util.ShareUtil
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareAndClipboardUtilTest {

    @Test
    fun formatInvoiceText_containsInvoiceNumberCustomerAndItems() {
        val invoice = Invoice(
            id = "inv_001",
            invoiceNumber = "INV-2026-001",
            customerName = "Budi Santoso",
            customerPhone = "081234567890",
            items = listOf(
                OrderItem("1", "p1", "Kopi Susu Aren", 2, 18000.0),
                OrderItem("2", "p2", "Roti Bakar Cokelat", 1, 15000.0)
            ),
            subtotal = 51000.0,
            discount = 5000.0,
            totalAmount = 56000.0,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = "QRIS",
            shippingInfo = ShippingInfo(
                recipientName = "Budi Santoso",
                phone = "081234567890",
                address = "Jl. Merdeka No. 10, Jakarta",
                courier = "GoSend",
                shippingFee = 10000.0
            ),
            notes = "Tolong kirim sebelum jam 12"
        )

        val formattedText = ShareUtil.formatInvoiceText(invoice)

        assertTrue(formattedText.contains("INV-2026-001"))
        assertTrue(formattedText.contains("Budi Santoso"))
        assertTrue(formattedText.contains("Kopi Susu Aren (2x)"))
        assertTrue(formattedText.contains("LUNAS ✅"))
        assertTrue(formattedText.contains("Jl. Merdeka No. 10, Jakarta"))
        assertTrue(formattedText.contains("GoSend"))
        assertTrue(formattedText.contains("Tolong kirim sebelum jam 12"))
    }

    @Test
    fun formatShippingLabel_containsCourierAddressAndPhone() {
        val invoice = Invoice(
            id = "inv_002",
            invoiceNumber = "INV-2026-002",
            customerName = "Siti Rahma",
            customerPhone = "089876543210",
            totalAmount = 45000.0,
            shippingInfo = ShippingInfo(
                recipientName = "Siti Rahma",
                phone = "089876543210",
                address = "Gedung Cyber Lt. 3, Kuningan",
                courier = "GrabExpress",
                trackingNumber = "GRAB-889900"
            )
        )

        val shippingLabelText = ShareUtil.formatShippingLabel(invoice)

        assertTrue(shippingLabelText.contains("LABEL PENGIRIMAN"))
        assertTrue(shippingLabelText.contains("Siti Rahma"))
        assertTrue(shippingLabelText.contains("089876543210"))
        assertTrue(shippingLabelText.contains("Gedung Cyber Lt. 3, Kuningan"))
        assertTrue(shippingLabelText.contains("GrabExpress"))
        assertTrue(shippingLabelText.contains("GRAB-889900"))
        assertTrue(shippingLabelText.contains("INV-2026-002"))
    }

    @Test
    fun formatQrisPaymentSummary_containsTotalNmidAndBankInfo() {
        val invoice = Invoice(
            id = "inv_003",
            invoiceNumber = "INV-2026-003",
            customerName = "Dewi",
            totalAmount = 75000.0
        )

        val qrisSummary = ShareUtil.formatQrisPaymentSummary(invoice, "ID10202688899")

        assertTrue(qrisSummary.contains("PEMBAYARAN QRIS / TRANSFER BANK"))
        assertTrue(qrisSummary.contains("INV-2026-003"))
        assertTrue(qrisSummary.contains("Dewi"))
        assertTrue(qrisSummary.contains("ID10202688899"))
        assertTrue(qrisSummary.contains("BCA: 8830-192-888"))
        assertTrue(qrisSummary.contains("Mandiri: 127-00-9988776-1"))
    }

    @Test
    fun formatPromoCaptionText_combinesTextCtaAndHashtags() {
        val caption = PromoCaption(
            id = "cap_001",
            productName = "Kopi Susu Aren",
            targetPlatform = SocialPlatform.INSTAGRAM,
            tone = CaptionTone.CASUAL,
            captionText = "Nikmati kesegaran Kopi Susu Aren asli hari ini!",
            callToAction = "Pesan sekarang lewat WhatsApp Kopitoko!",
            hashtags = listOf("#Kopitoko", "#KopiSusuAren", "#KulinerJakarta")
        )

        val formattedCaption = ShareUtil.formatPromoCaptionText(caption)

        assertTrue(formattedCaption.startsWith("Nikmati kesegaran Kopi Susu Aren asli hari ini!"))
        assertTrue(formattedCaption.contains("👉 Pesan sekarang lewat WhatsApp Kopitoko!"))
        assertTrue(formattedCaption.contains("#Kopitoko #KopiSusuAren #KulinerJakarta"))
    }
}
