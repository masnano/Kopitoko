package com.android.kopitoko.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kopitoko.data.ClipboardObserver
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.OrderItem
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.domain.model.ShippingInfo
import com.android.kopitoko.ui.architecture.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val geminiRepository: GeminiRepository,
    private var clipboardObserver: ClipboardObserver? = null
) : ViewModel() {

    private val _invoiceListState = MutableStateFlow<UiState<List<Invoice>>>(
        UiState.Success(
            listOf(
                Invoice(
                    id = "inv_1",
                    invoiceNumber = "INV-20260501-001",
                    customerName = "Budi Santoso",
                    customerPhone = "081234567890",
                    items = listOf(
                        OrderItem(id = "item_1", productId = "p1", productName = "Kopi Susu Gula Aren", quantity = 2, unitPrice = 18000.0),
                        OrderItem(id = "item_2", productId = "p2", productName = "Croissant Cokelat", quantity = 1, unitPrice = 22000.0)
                    ),
                    subtotal = 58000.0,
                    totalAmount = 58000.0,
                    paymentStatus = PaymentStatus.PAID,
                    paymentMethod = "QRIS",
                    createdAt = System.currentTimeMillis() - 86400000L,
                    shippingInfo = ShippingInfo(
                        recipientName = "Budi Santoso",
                        phone = "081234567890",
                        address = "Jl. Sudirman No. 45, Jakarta",
                        courier = "GoSend",
                        shippingFee = 10000.0
                    )
                )
            )
        )
    )
    val invoiceListState: StateFlow<UiState<List<Invoice>>> = _invoiceListState.asStateFlow()

    private val _ocrState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val ocrState: StateFlow<UiState<String>> = _ocrState.asStateFlow()

    private val _detectedClipboardText = MutableStateFlow<String?>(null)
    val detectedClipboardText: StateFlow<String?> = _detectedClipboardText.asStateFlow()

    private val _chatParsingState = MutableStateFlow<UiState<Invoice>>(UiState.Empty)
    val chatParsingState: StateFlow<UiState<Invoice>> = _chatParsingState.asStateFlow()

    private val _editingInvoice = MutableStateFlow<Invoice?>(null)
    val editingInvoice: StateFlow<Invoice?> = _editingInvoice.asStateFlow()

    private val _showParseDialog = MutableStateFlow(false)
    val showParseDialog: StateFlow<Boolean> = _showParseDialog.asStateFlow()

    fun setClipboardObserver(observer: ClipboardObserver) {
        this.clipboardObserver = observer
    }

    fun checkClipboard(context: Context) {
        val observer = clipboardObserver ?: ClipboardObserver(context).also { clipboardObserver = it }
        val text = observer.getClipboardText()
        if (text != null && observer.isOrderChatText(text)) {
            _detectedClipboardText.value = text
        }
    }

    fun checkClipboardText(text: String?) {
        val observer = clipboardObserver
        if (text != null && (observer?.isOrderChatText(text) == true || isBasicOrderText(text))) {
            _detectedClipboardText.value = text
        }
    }

    private fun isBasicOrderText(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("pesan") || lower.contains("order") || lower.contains("nama") || lower.contains("alamat")
    }

    fun dismissClipboardBanner() {
        _detectedClipboardText.value = null
    }

    fun openParseDialog(initialText: String? = null) {
        _showParseDialog.value = true
        if (initialText != null) {
            parseChatToInvoice(initialText)
        }
    }

    fun closeParseDialog() {
        _showParseDialog.value = false
        _chatParsingState.value = UiState.Empty
    }

    fun parseChatToInvoice(chatText: String) {
        viewModelScope.launch {
            _chatParsingState.value = UiState.Loading
            geminiRepository.parseChatToInvoice(chatText).fold(
                onSuccess = { invoice ->
                    _chatParsingState.value = UiState.Success(invoice)
                    _editingInvoice.value = invoice
                },
                onFailure = { error ->
                    _chatParsingState.value = UiState.Error(
                        error.localizedMessage ?: "Failed to parse chat to invoice."
                    )
                }
            )
        }
    }

    fun updateEditingInvoice(updatedInvoice: Invoice) {
        val subtotal = updatedInvoice.items.sumOf { it.totalPrice }
        val shippingFee = updatedInvoice.shippingInfo?.shippingFee ?: 0.0
        val discount = updatedInvoice.discount
        val totalAmount = maxOf(0.0, subtotal + shippingFee - discount)

        _editingInvoice.value = updatedInvoice.copy(
            subtotal = subtotal,
            totalAmount = totalAmount
        )
    }

    fun saveEditingInvoice() {
        val invoice = _editingInvoice.value ?: return
        addInvoice(invoice)
        _editingInvoice.value = null
        _chatParsingState.value = UiState.Empty
        _showParseDialog.value = false
        _detectedClipboardText.value = null
    }

    fun cancelEditingInvoice() {
        _editingInvoice.value = null
        _chatParsingState.value = UiState.Empty
    }

    fun parseInvoiceFromImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _ocrState.value = UiState.Loading
            val prompt = """
                Analyze this receipt/invoice image and extract the following in JSON format:
                - customerName
                - items (list of {productName, quantity, unitPrice})
                - totalAmount
                - paymentStatus
            """.trimIndent()

            val result = geminiRepository.generateMultimodal(prompt, bitmap)
            result.fold(
                onSuccess = { parsedText ->
                    _ocrState.value = UiState.Success(parsedText)
                },
                onFailure = { error ->
                    _ocrState.value = UiState.Error(
                        error.localizedMessage ?: "Failed to process receipt image."
                    )
                }
            )
        }
    }

    fun addInvoice(invoice: Invoice) {
        val currentList = (_invoiceListState.value as? UiState.Success)?.data ?: emptyList()
        _invoiceListState.value = UiState.Success(listOf(invoice) + currentList)
    }
}
