package com.android.kopitoko.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SocialPlatform {
    INSTAGRAM,
    WHATSAPP,
    TIKTOK,
    FACEBOOK
}

@Serializable
enum class CaptionTone {
    CASUAL,
    PROFESSIONAL,
    URGENT_PROMO,
    FRIENDLY,
    STORYTELLING
}

@Serializable
data class PromoCaption(
    val id: String,
    val productId: String? = null,
    val productName: String? = null,
    val targetPlatform: SocialPlatform = SocialPlatform.INSTAGRAM,
    val tone: CaptionTone = CaptionTone.CASUAL,
    val captionText: String,
    val hashtags: List<String> = emptyList(),
    val callToAction: String? = null,
    val generatedAt: Long = System.currentTimeMillis()
)
