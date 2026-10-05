# Project Plan

Micro-Merchant AI Copilot (Katalog, Rekap Pesanan & Konten Otomatis) bagi pelaku UMKM / penjual online.
Fitur utama:
1. Smart Chat-to-Invoice: Salin teks chat pesanan, AI otomatis mengubahnya menjadi format rincian tagihan (invoice), label pengiriman ekspedisi, dan QRIS dinamis.
2. Instant Product Photo Enhancer: Mengubah foto produk sederhana menjadi foto katalog profesional dengan studio background / enhancer.
3. Caption & Promo Generator: Menghasilkan deskripsi produk dan caption media sosial berdasarkan foto / deskripsi produk.
Teknologi: Jetpack Compose, CameraX, Generative AI / Gemini API, Clipboard Listener.

## Project Brief

# Project Brief: Micro-Merchant AI Copilot

## Features
1. **Smart Chat-to-Invoice**: Automatically detects copied order chat text from the clipboard and uses AI to transform it into structured order summaries, shipping labels, and payment breakdown details.
2. **Instant Product Photo Enhancer**: Captures product photos directly using an in-app camera and applies AI studio backgrounds and enhancements for professional product displays.
3. **Caption & Promo Generator**: Automatically generates engaging product descriptions, promotional social media captions, and relevant hashtags based on product photos and quick details.
4. **Quick Copy & Share Hub**: Formats generated invoices, promo text, and enhanced photos into lightweight cards ready for instant 1-click sharing to customer messaging apps.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Navigation**: Jetpack Navigation 3 (state-driven)
- **Adaptive Strategy**: Compose Material Adaptive library (`androidx.compose.material3.adaptive`)
- **Architecture & Concurrency**: ViewModel, StateFlow, Kotlin Coroutines, Flow
- **Generative AI**: Google Gemini API / GenAI SDK (for chat parsing, promo caption generation, and vision processing)
- **Camera & Media Handling**: CameraX (photo capture) and Coil (image loading)
- **System Integration**: Android `ClipboardManager` (order chat detection)

## Implementation Steps
**Total Duration:** 2h 5s

### Task_1_Core_Architecture_Navigation_And_Gemini_Setup: Configure Jetpack Navigation / M3 Adaptive layout, Gemini AI SDK integration with API_KEY support, and core domain models (Invoice, Caption, Product).
- **Status:** COMPLETED
- **Updates:** Successfully set up Gemini AI SDK integration, BuildConfig API key fallback, ApiKeyRepository, domain models (Invoice, Product, PromoCaption, ShippingInfo), ViewModels (MainViewModel, ApiKeyViewModel, CaptionViewModel, InvoiceViewModel, ProductViewModel), Material 3 adaptive navigation layout (KopitokoApp), and verified build & unit tests.
- **Acceptance Criteria:**
  - Project builds successfully
  - Gemini API_KEY integration configured and functional
  - Main navigation and ViewModel state architecture established
- **Duration:** 10m 26s

### Task_2_Smart_Chat_To_Invoice_Feature: Implement clipboard order chat detection using Android ClipboardManager and Gemini AI prompt parsing to generate structured invoices with items, prices, and shipping details.
- **Status:** COMPLETED
- **Updates:** Implemented ClipboardObserver for automatic clipboard chat order detection, Gemini 1.5 Flash structured JSON chat-to-invoice parsing with robust offline heuristic fallback, full editable invoice UI dialogs, ClipboardOrderBanner on HomeScreen and InvoicesScreen, and verified with unit tests and assembleDebug.
- **Acceptance Criteria:**
  - Clipboard order chat detection active and functional
  - Gemini API parses chat text into structured invoice cards
  - UI allows viewing and editing extracted invoice fields
- **Duration:** 7m 34s

### Task_3_CameraX_Photo_Enhancer_And_Caption_Generator: Integrate CameraX for product photo capture, visual preview enhancer, and Gemini AI caption & hashtag generator based on product photo details.
- **Status:** COMPLETED
- **Updates:** Implemented CameraX photo capture with permissions & gallery fallbacks, ProductEnhancerScreen with 5 studio backdrops (Clean White, Wood Aesthetic, Soft Pastel, Minimalist Marble, Warm Coffee) and custom Canvas rendering engine, Gemini 1.5 Flash multimodal caption and hashtag generator with tone and platform controls, and verified with unit tests and assembleDebug.
- **Acceptance Criteria:**
  - CameraX photo capture and preview working
  - Gemini AI generates social media captions, descriptions, and hashtags
  - UI renders product enhancer background options
- **Duration:** 42m 41s

### Task_4_Quick_Copy_And_Share_Hub: Build Quick Copy & Share Hub for 1-click sharing of formatted invoices, promotional captions, and enhanced photos via Android Intents and Clipboard.
- **Status:** COMPLETED
- **Updates:** Implemented ShareUtil and ClipboardUtil for 1-click text/image sharing, dynamic QRIS text, expedition shipping labels, ShareHubBottomSheet and EmbeddedQuickShareHubCard UI across screens, FileProvider configuration in AndroidManifest.xml, and verified with unit tests and assembleDebug.
- **Acceptance Criteria:**
  - 1-click quick copy to clipboard for captions and invoice text
  - Android Intent sharing for invoices, captions, and images to external apps works
- **Duration:** 6m 32s

### Task_5_Run_And_Verify: Run and verify the complete Kopitoko application. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Deployed and verified the Kopitoko application on emulator device (emulator-5554). All 7 core test areas (App launch, Navigation, Smart Chat-to-Invoice, Photo Studio & CameraX, Caption Generator, Quick Copy & Share Hub, Settings & Gemini API key) passed without any crashes, errors, or UI issues. Build and unit tests passed completely.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - All app features verified and operational
- **Duration:** 52m 52s

