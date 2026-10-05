package com.android.kopitoko.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ShippingStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}

@Serializable
data class ShippingInfo(
    val recipientName: String,
    val phone: String,
    val address: String,
    val courier: String = "GoSend",
    val trackingNumber: String? = null,
    val shippingFee: Double = 0.0,
    val status: ShippingStatus = ShippingStatus.PENDING
)
