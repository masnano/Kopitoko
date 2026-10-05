package com.android.kopitoko

import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.OrderItem
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.domain.model.Product
import com.android.kopitoko.domain.model.PromoCaption
import com.android.kopitoko.domain.model.SocialPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DomainAndRepositoryTest {

    @Test
    fun orderItem_totalPriceCalculation_isCorrect() {
        val item = OrderItem(
            id = "item_1",
            productId = "p1",
            productName = "Kopi Susu Aren",
            quantity = 3,
            unitPrice = 18000.0
        )
        assertEquals(54000.0, item.totalPrice, 0.01)
    }

    @Test
    fun invoiceCreation_hasValidData() {
        val invoice = Invoice(
            id = "inv_1",
            invoiceNumber = "INV-001",
            customerName = "Budi",
            totalAmount = 54000.0,
            paymentStatus = PaymentStatus.PAID
        )
        assertEquals("INV-001", invoice.invoiceNumber)
        assertEquals(PaymentStatus.PAID, invoice.paymentStatus)
    }

    @Test
    fun productAndCaptionModel_instantiation_isSuccessful() {
        val product = Product(
            id = "p1",
            name = "Americano",
            price = 15000.0
        )
        val caption = PromoCaption(
            id = "c1",
            productId = product.id,
            captionText = "Best Americano in town!",
            targetPlatform = SocialPlatform.INSTAGRAM
        )

        assertNotNull(product)
        assertEquals("Americano", product.name)
        assertEquals(SocialPlatform.INSTAGRAM, caption.targetPlatform)
    }
}
