package com.android.kopitoko

import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.domain.model.CaptionTone
import com.android.kopitoko.domain.model.SocialPlatform
import com.android.kopitoko.domain.model.StudioBackgroundTheme
import com.android.kopitoko.domain.model.StudioBackgroundType
import com.android.kopitoko.ui.architecture.UiState
import com.android.kopitoko.ui.viewmodel.CaptionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraAndPhotoEnhancerTest {

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
    fun studioBackgroundThemes_areAllDefined() {
        val themes = StudioBackgroundTheme.ALL_THEMES
        assertEquals(5, themes.size)
        assertTrue(themes.any { it.type == StudioBackgroundType.CLEAN_STUDIO_WHITE })
        assertTrue(themes.any { it.type == StudioBackgroundType.WOOD_TABLE_AESTHETIC })
        assertTrue(themes.any { it.type == StudioBackgroundType.SOFT_PASTEL_PINK })
        assertTrue(themes.any { it.type == StudioBackgroundType.MINIMALIST_MARBLE })
        assertTrue(themes.any { it.type == StudioBackgroundType.WARM_COFFEE_SHOP })
    }

    @Test
    fun geminiRepository_generateCaptionAndHashtags_producesPromoCaptionAndHashtags() = runTest {
        val apiKeyRepository = FakeApiKeyRepository("")
        val repository = GeminiRepository(apiKeyRepository)

        val result = repository.generateCaptionAndHashtags(
            bitmap = null,
            tone = "URGENT_PROMO",
            extraNotes = "Beli 1 Gratis 1",
            productName = "Kopi Susu Aren",
            platform = SocialPlatform.INSTAGRAM
        )

        assertTrue(result.isSuccess)
        val caption = result.getOrNull()
        assertNotNull(caption)
        assertEquals("Kopi Susu Aren", caption?.productName)
        assertEquals(SocialPlatform.INSTAGRAM, caption?.targetPlatform)
        assertEquals(CaptionTone.URGENT_PROMO, caption?.tone)
        assertTrue(caption?.captionText?.contains("PROMO SPESIAL") == true)
        assertTrue(caption?.hashtags?.isNotEmpty() == true)
    }

    @Test
    fun captionViewModel_photoSelectionAndGeneration_updatesState() = runTest {
        val apiKeyRepository = FakeApiKeyRepository("")
        val repository = GeminiRepository(apiKeyRepository)
        val viewModel = CaptionViewModel(repository)

        assertNull(viewModel.selectedBitmap.value)

        viewModel.generateCaptionWithPhoto(
            productName = "Croissant Cokelat",
            details = "Diskon 20%",
            platform = SocialPlatform.WHATSAPP,
            tone = CaptionTone.FRIENDLY
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.captionState.value
        assertTrue(state is UiState.Success)
        val promoCaption = (state as UiState.Success).data
        assertEquals("Croissant Cokelat", promoCaption.productName)
        assertEquals(SocialPlatform.WHATSAPP, promoCaption.targetPlatform)
        assertTrue(viewModel.recentCaptions.value.contains(promoCaption))
    }
}
