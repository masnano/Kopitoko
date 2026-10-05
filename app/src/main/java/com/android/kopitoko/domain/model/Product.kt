package com.android.kopitoko.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val costPrice: Double? = null,
    val stockQuantity: Int = 0,
    val category: String = "General",
    val imageUrl: String? = null,
    val sku: String? = null,
    val isAvailable: Boolean = true
)
