package com.android.kopitoko.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus {
    UNPAID,
    PARTIALLY_PAID,
    PAID,
    REFUNDED,
    CANCELLED
}

@Serializable
data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String? = null,
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val shippingInfo: ShippingInfo? = null,
    val notes: String? = null
)
