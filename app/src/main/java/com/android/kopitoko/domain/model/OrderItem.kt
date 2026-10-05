package com.android.kopitoko.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String? = null
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}
