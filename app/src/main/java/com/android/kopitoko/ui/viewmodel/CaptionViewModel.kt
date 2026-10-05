package com.android.kopitoko.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.domain.model.CaptionTone
import com.android.kopitoko.domain.model.PromoCaption
import com.android.kopitoko.domain.model.SocialPlatform
import com.android.kopitoko.ui.architecture.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptionViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _captionState = MutableStateFlow<UiState<PromoCaption>>(UiState.Empty)
    val captionState: StateFlow<UiState<PromoCaption>> = _captionState.asStateFlow()

    private val _recentCaptions = MutableStateFlow<List<PromoCaption>>(emptyList())
    val recentCaptions: StateFlow<List<PromoCaption>> = _recentCaptions.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    fun setSelectedBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
    }

    fun clearSelectedBitmap() {
        _selectedBitmap.value = null
    }

    fun generateCaption(
        productName: String,
        details: String,
        platform: SocialPlatform,
        tone: CaptionTone
    ) {
        generateCaptionWithPhoto(
            productName = productName,
            details = details,
            platform = platform,
            tone = tone,
            bitmap = _selectedBitmap.value
        )
    }

    fun generateCaptionWithPhoto(
        productName: String,
        details: String,
        platform: SocialPlatform,
        tone: CaptionTone,
        bitmap: Bitmap? = _selectedBitmap.value
    ) {
        viewModelScope.launch {
            _captionState.value = UiState.Loading
            val result = geminiRepository.generateCaptionAndHashtags(
                bitmap = bitmap,
                tone = tone.name,
                extraNotes = details,
                productName = productName.ifBlank { "Produk Kopitoko" },
                platform = platform
            )

            result.fold(
                onSuccess = { promoCaption ->
                    _captionState.value = UiState.Success(promoCaption)
                    _recentCaptions.value = listOf(promoCaption) + _recentCaptions.value
                },
                onFailure = { error ->
                    _captionState.value = UiState.Error(
                        message = error.localizedMessage ?: "Failed to generate promo caption."
                    )
                }
            )
        }
    }

    fun resetState() {
        _captionState.value = UiState.Empty
    }
}
