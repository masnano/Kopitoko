package com.android.kopitoko.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.domain.model.Product
import com.android.kopitoko.ui.architecture.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProductViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<Product>>>(
        UiState.Success(
            listOf(
                Product(id = "p1", name = "Kopi Susu Gula Aren", description = "Espresso blend dengan susu segar dan gula aren asli", price = 18000.0, stockQuantity = 50, category = "Beverage"),
                Product(id = "p2", name = "Americano Ice", description = "Double shot espresso dengan air dingin dan es", price = 15000.0, stockQuantity = 100, category = "Beverage"),
                Product(id = "p3", name = "Croissant Cokelat", description = "Pastry renyah dengan isian cokelat lumer", price = 22000.0, stockQuantity = 20, category = "Snack")
            )
        )
    )
    val productsState: StateFlow<UiState<List<Product>>> = _productsState.asStateFlow()

    private val _descriptionGenState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val descriptionGenState: StateFlow<UiState<String>> = _descriptionGenState.asStateFlow()

    private val _stagedBitmap = MutableStateFlow<Bitmap?>(null)
    val stagedBitmap: StateFlow<Bitmap?> = _stagedBitmap.asStateFlow()

    fun setStagedBitmap(bitmap: Bitmap?) {
        _stagedBitmap.value = bitmap
    }

    fun clearStagedBitmap() {
        _stagedBitmap.value = null
    }

    fun generateProductDescription(productName: String, keywords: String) {
        viewModelScope.launch {
            _descriptionGenState.value = UiState.Loading
            val prompt = "Write an enticing 2-sentence product description in Indonesian for a product named '$productName' with key traits: $keywords"
            val result = geminiRepository.generateText(prompt)
            result.fold(
                onSuccess = { text ->
                    _descriptionGenState.value = UiState.Success(text)
                },
                onFailure = { error ->
                    _descriptionGenState.value = UiState.Error(error.localizedMessage ?: "Failed to generate description.")
                }
            )
        }
    }

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        category: String = "General",
        stockQuantity: Int = 10,
        imageUrl: String? = null
    ) {
        val newProduct = Product(
            id = "prod_${UUID.randomUUID().toString().take(8)}",
            name = name,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            category = category,
            imageUrl = imageUrl
        )
        addProduct(newProduct)
    }

    fun addProduct(product: Product) {
        val currentList = (_productsState.value as? UiState.Success)?.data ?: emptyList()
        _productsState.value = UiState.Success(listOf(product) + currentList)
    }
}
