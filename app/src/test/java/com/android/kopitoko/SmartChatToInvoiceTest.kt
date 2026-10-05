package com.android.kopitoko

import com.android.kopitoko.data.ClipboardObserver
import com.android.kopitoko.data.repository.ApiKeyRepository
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.ui.architecture.UiState
import com.android.kopitoko.ui.viewmodel.InvoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeApiKeyRepository(key: String = "") : ApiKeyRepository(null) {
    override val apiKeyFlow: Flow<String> = flowOf(key)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SmartChatToInvoiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isOrderChatText_detectsOrderKeywordsAndPrices() {
        val observer = ClipboardObserver()

        val validOrderText = """
            Halo kak, mau order:
            2x Kopi Susu Aren @ 18000
            Nama: Budi Santoso
            Alamat: Jl. Merdeka No. 10
            Ongkir: 10000
            Sudah Lunas via QRIS
        """.trimIndent()

        val regularText = "Halo bro, besok jam berapa ketemuan?"

        assertTrue(observer.isOrderChatText(validOrderText))
        assertFalse(observer.isOrderChatText(regularText))
    }

    @Test
    fun fallbackParseChatToInvoice_extractsCustomerAndItemsAccurately() {
        val apiKeyRepository = FakeApiKeyRepository("")
        val geminiRepository = GeminiRepository(apiKeyRepository)

        val chatText = """
            Halo, mau pesan:
            2x Kopi Susu Aren @ 18000
            1x Croissant Cokelat @ 22000
            Nama: Siti Rahma
            No HP: 081234567890
            Alamat: Jl. Sudirman No 45, Jakarta
            Ekspedisi: GoSend
            Ongkir: 10000
            Diskon: 5000
            Metode: QRIS Lunas
        """.trimIndent()

        val invoice = geminiRepository.fallbackParseChatToInvoice(chatText)

        assertEquals("Siti Rahma", invoice.customerName)
        assertEquals("081234567890", invoice.customerPhone)
        assertEquals("Jl. Sudirman No 45, Jakarta", invoice.shippingInfo?.address)
        assertEquals("GoSend", invoice.shippingInfo?.courier)
        assertEquals(10000.0, invoice.shippingInfo?.shippingFee ?: 0.0, 0.01)
        assertEquals(5000.0, invoice.discount, 0.01)
        assertEquals(PaymentStatus.PAID, invoice.paymentStatus)
        assertEquals(2, invoice.items.size)
        assertEquals(58000.0, invoice.subtotal, 0.01) // (2*18000) + (1*22000)
        assertEquals(63000.0, invoice.totalAmount, 0.01) // 58000 + 10000 - 5000
    }

    @Test
    fun invoiceViewModel_parseAndSaveEditingInvoice_addsToInvoiceList() = runTest {
        val apiKeyRepository = FakeApiKeyRepository("")
        val geminiRepository = GeminiRepository(apiKeyRepository)
        val viewModel = InvoiceViewModel(geminiRepository)

        val chatText = """
            Pesan: 1x Kopi Susu @ 18000
            Nama: Ahmad
            Alamat: Jl. Melati No 5
        """.trimIndent()

        viewModel.parseChatToInvoice(chatText)
        testDispatcher.scheduler.advanceUntilIdle()

        val editingInv = viewModel.editingInvoice.value
        assertNotNull(editingInv)
        assertEquals("Ahmad", editingInv?.customerName)

        val updated = editingInv!!.copy(customerName = "Ahmad Dahlan")
        viewModel.updateEditingInvoice(updated)
        viewModel.saveEditingInvoice()

        val listState = viewModel.invoiceListState.value
        assertTrue(listState is UiState.Success)
        val invoices = (listState as UiState.Success).data
        assertTrue(invoices.any { it.customerName == "Ahmad Dahlan" })
    }
}
