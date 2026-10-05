package com.android.kopitoko.data.repository

import android.graphics.Bitmap
import com.android.kopitoko.domain.model.CaptionTone
import com.android.kopitoko.domain.model.Invoice
import com.android.kopitoko.domain.model.OrderItem
import com.android.kopitoko.domain.model.PaymentStatus
import com.android.kopitoko.domain.model.PromoCaption
import com.android.kopitoko.domain.model.ShippingInfo
import com.android.kopitoko.domain.model.SocialPlatform
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GeminiRepository(private val apiKeyRepository: ApiKeyRepository) {

    companion object {
        const val MODEL_TEXT = "gemini-1.5-flash"
        const val MODEL_MULTIMODAL = "gemini-1.5-flash"
    }

    private suspend fun getActiveKey(overrideKey: String?): String {
        if (!overrideKey.isNullOrBlank()) {
            return overrideKey.trim()
        }
        return apiKeyRepository.apiKeyFlow.first().trim()
    }

    suspend fun generateText(
        prompt: String,
        customKey: String? = null,
        modelName: String = MODEL_TEXT
    ): Result<String> {
        return runCatching {
            val key = getActiveKey(customKey)
            if (key.isBlank()) {
                throw IllegalStateException("Gemini API Key is not configured. Please set an API Key in Settings.")
            }
            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = key
            )
            val response = generativeModel.generateContent(prompt)
            response.text ?: throw IllegalStateException("Received empty response from Gemini AI.")
        }
    }

    suspend fun generateMultimodal(
        prompt: String,
        bitmap: Bitmap,
        customKey: String? = null,
        modelName: String = MODEL_MULTIMODAL
    ): Result<String> {
        return runCatching {
            val key = getActiveKey(customKey)
            if (key.isBlank()) {
                throw IllegalStateException("Gemini API Key is not configured. Please set an API Key in Settings.")
            }
            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = key
            )
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = generativeModel.generateContent(inputContent)
            response.text ?: throw IllegalStateException("Received empty response from Gemini AI.")
        }
    }

    suspend fun generateCaptionAndHashtags(
        bitmap: Bitmap?,
        tone: String,
        extraNotes: String,
        productName: String? = null,
        platform: SocialPlatform = SocialPlatform.INSTAGRAM,
        customKey: String? = null
    ): Result<PromoCaption> {
        return runCatching {
            val key = runCatching { getActiveKey(customKey) }.getOrDefault("")

            val promptText = """
                You are Kopitoko AI Copilot, a social media copywriter for micro-merchants and coffee shops in Indonesia.
                ${if (bitmap != null) "Analyze the visual details of this product photo (colors, packaging, presentation, appeal)." else "Create a promo post for this product."}
                
                Target Platform: $platform
                Product Name: ${productName ?: "Product in photo"}
                Extra Promo Notes / Price: $extraNotes
                Desired Tone: $tone
                
                Write an engaging Indonesian caption with:
                1. Catchy headline / hook
                2. Enticing product description highlighting key features and visual appeal
                3. Clear Call to Action (CTA)
                4. 5-8 trending Indonesian small business & coffee hashtags (e.g. #Kuliner #UMKMBisa #KopiKopi #Kopitoko #PromoHariIni)
            """.trimIndent()

            val responseText = if (bitmap != null && key.isNotBlank()) {
                generateMultimodal(promptText, bitmap, customKey = key).getOrElse {
                    generateText(promptText, customKey = key).getOrElse {
                        fallbackPromptResponse(productName, tone, extraNotes, platform)
                    }
                }
            } else if (key.isNotBlank()) {
                generateText(promptText, customKey = key).getOrElse {
                    fallbackPromptResponse(productName, tone, extraNotes, platform)
                }
            } else {
                fallbackPromptResponse(productName, tone, extraNotes, platform)
            }

            val hashtags = Regex("#\\w+").findAll(responseText).map { it.value }.toList()
                .ifEmpty { listOf("#Kopitoko", "#UMKMBisa", "#KulinerLokal", "#PromoHariIni", "#KopiKopi") }

            PromoCaption(
                id = UUID.randomUUID().toString(),
                productName = productName ?: "Produk Kopitoko",
                targetPlatform = platform,
                tone = parseCaptionTone(tone),
                captionText = responseText,
                hashtags = hashtags,
                callToAction = "Pesan sekarang via WhatsApp / Link Bio!",
                generatedAt = System.currentTimeMillis()
            )
        }
    }

    private fun fallbackPromptResponse(
        productName: String?,
        tone: String,
        extraNotes: String,
        platform: SocialPlatform
    ): String {
        val name = if (!productName.isNullOrBlank()) productName else "Produk Spesial Kopitoko"
        val notesText = if (extraNotes.isNotBlank()) "\n📌 Info Promo: $extraNotes" else ""
        val platformText = "Post khusus untuk ${platform.name}"

        return when (tone.lowercase()) {
            "promo", "urgent_promo", "diskon" -> """
                🔥 PROMO SPESIAL HARI INI! 🔥 ($platformText)
                
                Buat kamu pecinta $name, jangan sampai kelewatan promo terbatas dari Kopitoko!
                Dibuat dari bahan-bahan terbaik, pas banget buat nemenin harimu.$notesText
                
                🛒 Buruan pesan sekarang sebelum kehabisan!
                👇 Klik link di bio / Chat WA Kopitoko!
                
                #Kopitoko #PromoHariIni #KulinerKekinian #UMKMBisa #DiskonSpesial #KopiKopi
            """.trimIndent()

            "professional" -> """
                Menyajikan Kualitas Terbaik untuk Anda.
                
                $name hadir dengan standar rasa dan penyajian terbaik dari Kopitoko.
                Sempurna untuk menemani produktivitas kerja dan momen spesial Anda.$notesText
                
                Pesan langsung melalui WhatsApp Kopitoko untuk layanan cepat.
                
                #Kopitoko #KualitasTerbaik #KulinerIndonesia #UMKMBisa #ProfessionalService
            """.trimIndent()

            "aesthetic", "storytelling" -> """
                Setiap detail punya ceritanya sendiri ✨
                
                Menikmati $name di tengah hiruk pikuk hari adalah bentuk self-reward paling manis. Dibuat penuh rasa dan ketelitian khusus untuk kamu.$notesText
                
                Yuk ciptakan momen serumu hari ini bersama Kopitoko!
                
                #Kopitoko #AestheticVibes #ChillMoment #KulinerHits #CoffeeAndMore
            """.trimIndent()

            else -> """
                Halo Kopilovers! 👋
                
                Lagi nyari yang segar & nikmat? Yuk cobain $name dari Kopitoko!
                Racikan khas dengan bahan premium yang siap bikin mood kamu balik lagi.$notesText
                
                Langsung order via WhatsApp Kopitoko ya!
                
                #Kopitoko #KulinerLokal #UMKMBisa #KopiKopi #RasaMantap
            """.trimIndent()
        }
    }

    private fun parseCaptionTone(toneStr: String): CaptionTone {
        return runCatching {
            CaptionTone.valueOf(toneStr.uppercase())
        }.getOrDefault(CaptionTone.CASUAL)
    }

    suspend fun parseChatToInvoice(
        chatText: String,
        customKey: String? = null
    ): Result<Invoice> {
        return runCatching {
            val key = runCatching { getActiveKey(customKey) }.getOrDefault("")
            if (key.isBlank()) {
                return Result.success(fallbackParseChatToInvoice(chatText))
            }

            val prompt = """
                You are an AI assistant for a micro-merchant shop in Indonesia (Kopitoko).
                Analyze the following customer chat message and extract the order details into a structured JSON object.

                Chat message:
                "$chatText"

                Respond ONLY with a valid JSON object matching this schema (no markdown formatting, no code block fence, no extra commentary):
                {
                  "customerName": "Customer Name",
                  "customerPhone": "08xxxxxxxxxx or null",
                  "address": "Full delivery address or null",
                  "courier": "Expedition courier service (e.g. GoSend, JNE, J&T, SiCepat, GrabExpress)",
                  "shippingFee": 10000.0,
                  "discount": 0.0,
                  "paymentStatus": "PAID or UNPAID",
                  "paymentMethod": "QRIS or Transfer or Cash",
                  "notes": "Any special notes or null",
                  "items": [
                    {
                      "productName": "Item Name",
                      "quantity": 1,
                      "unitPrice": 15000.0
                    }
                  ]
                }
            """.trimIndent()

            val generativeModel = GenerativeModel(
                modelName = MODEL_TEXT,
                apiKey = key
            )
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")
            parseInvoiceJson(responseText, chatText)
        }.recoverCatching {
            fallbackParseChatToInvoice(chatText)
        }
    }

    private fun parseInvoiceJson(jsonText: String, originalText: String): Invoice {
        val cleaned = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val obj = JSONObject(cleaned)
        val customerName = obj.optString("customerName", "").ifBlank { "Pelanggan" }
        val customerPhone = obj.optString("customerPhone", "").ifBlank { null }
        val address = obj.optString("address", "").ifBlank { null }
        val courier = obj.optString("courier", "GoSend").ifBlank { "GoSend" }
        val shippingFee = obj.optDouble("shippingFee", 0.0).let { if (it.isNaN()) 0.0 else it }
        val discount = obj.optDouble("discount", 0.0).let { if (it.isNaN()) 0.0 else it }

        val paymentStatusStr = obj.optString("paymentStatus", "UNPAID")
        val paymentStatus = if (paymentStatusStr.equals("PAID", ignoreCase = true) || paymentStatusStr.contains("LUNAS", ignoreCase = true)) {
            PaymentStatus.PAID
        } else {
            PaymentStatus.UNPAID
        }

        val paymentMethod = obj.optString("paymentMethod", "QRIS").ifBlank { "QRIS" }
        val notes = obj.optString("notes", "").ifBlank { null }

        val jsonItems = obj.optJSONArray("items")
        val itemsList = mutableListOf<OrderItem>()
        if (jsonItems != null) {
            for (i in 0 until jsonItems.length()) {
                val itemObj = jsonItems.getJSONObject(i)
                val pName = itemObj.optString("productName", "Item ${i + 1}")
                val qty = itemObj.optInt("quantity", 1)
                val price = itemObj.optDouble("unitPrice", 0.0)
                itemsList.add(
                    OrderItem(
                        id = "item_${System.currentTimeMillis()}_$i",
                        productId = "prod_${i + 1}",
                        productName = pName,
                        quantity = if (qty > 0) qty else 1,
                        unitPrice = if (price >= 0) price else 0.0
                    )
                )
            }
        }

        if (itemsList.isEmpty()) {
            return fallbackParseChatToInvoice(originalText)
        }

        val subtotal = itemsList.sumOf { it.totalPrice }
        val totalAmount = maxOf(0.0, subtotal + shippingFee - discount)

        return Invoice(
            id = "inv_${UUID.randomUUID().toString().take(8)}",
            invoiceNumber = generateInvoiceNumber(),
            customerName = customerName,
            customerPhone = customerPhone,
            items = itemsList,
            subtotal = subtotal,
            discount = discount,
            tax = 0.0,
            totalAmount = totalAmount,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            createdAt = System.currentTimeMillis(),
            shippingInfo = ShippingInfo(
                recipientName = customerName,
                phone = customerPhone ?: "",
                address = address ?: "Alamat Belum Diisi",
                courier = courier,
                shippingFee = shippingFee
            ),
            notes = notes
        )
    }

    fun fallbackParseChatToInvoice(chatText: String): Invoice {
        val lines = chatText.lines().map { it.trim() }.filter { it.isNotBlank() }

        var customerName = "Pelanggan"
        var customerPhone: String? = null
        var address: String? = null
        var courier = "GoSend"
        var shippingFee = 0.0
        var discount = 0.0
        var paymentMethod = "QRIS"
        var paymentStatus = PaymentStatus.UNPAID
        val notes: String? = null

        val items = mutableListOf<OrderItem>()

        // 1. Phone extraction
        val phoneRegex = Regex("""(?i)(?:hp|no hp|telp|phone|wa|whatsapp)\s*[:=-]?\s*([0-9+\s-]+)""")
        val rawPhoneRegex = Regex("""(?:\+62|0)8\d{8,11}""")

        // 2. Name extraction
        val nameRegex = Regex("""(?i)(?:nama|penerima|name|pelanggan|to)\s*[:=-]\s*(.+)""")

        // 3. Address extraction
        val addressRegex = Regex("""(?i)(?:alamat|address|kirim ke|lokasi)\s*[:=-]\s*(.+)""")

        // 4. Courier extraction
        val courierRegex = Regex("""(?i)(?:ekspedisi|kurir|courier|pengiriman)\s*[:=-]\s*(.+)""")

        // 5. Shipping fee extraction
        val shippingRegex = Regex("""(?i)(?:ongkir|biaya kirim|shipping)\s*[:=-]?\s*(?:rp\.?)?\s*([\d.,]+)""")

        // 6. Discount extraction
        val discountRegex = Regex("""(?i)(?:diskon|discount|potongan)\s*[:=-]?\s*(?:rp\.?)?\s*([\d.,]+)""")

        // Common courier names check
        val lowerText = chatText.lowercase()
        val couriers = listOf("JNE", "J&T", "JNT", "SiCepat", "GoSend", "GrabExpress", "Grab", "AnterAja", "Pos", "ShopeeXpress", "SPX")
        for (c in couriers) {
            if (lowerText.contains(c.lowercase())) {
                courier = if (c.lowercase() == "jnt") "J&T" else c
                break
            }
        }

        // Payment status & method check
        if (lowerText.contains("qris")) paymentMethod = "QRIS"
        else if (lowerText.contains("transfer") || lowerText.contains("bca") || lowerText.contains("mandiri") || lowerText.contains("bri")) paymentMethod = "Transfer"
        else if (lowerText.contains("cash") || lowerText.contains("tunai")) paymentMethod = "Cash"

        if (lowerText.contains("lunas") || lowerText.contains("paid") || lowerText.contains("sudah bayar") || lowerText.contains("sudah transfer")) {
            paymentStatus = PaymentStatus.PAID
        }

        for (line in lines) {
            nameRegex.find(line)?.let { customerName = it.groupValues[1].trim() }
            phoneRegex.find(line)?.let { customerPhone = cleanNumberString(it.groupValues[1]) }
            addressRegex.find(line)?.let { address = it.groupValues[1].trim() }
            courierRegex.find(line)?.let { courier = it.groupValues[1].trim() }
            shippingRegex.find(line)?.let { shippingFee = parseCurrencyDouble(it.groupValues[1]) }
            discountRegex.find(line)?.let { discount = parseCurrencyDouble(it.groupValues[1]) }

            if (customerPhone == null) {
                rawPhoneRegex.find(line)?.let { customerPhone = cleanNumberString(it.value) }
            }

            // Line items matching:
            // Pattern A: 2x Kopi Susu Aren @ 18000 or 2 Kopi Susu = 36000
            val itemPatternA = Regex("""(?i)^[-*]?\s*(\d+)\s*x?\s*([A-Za-z0-9\s()_-]+?)\s*[@=]\s*(?:rp\.?)?\s*([\d.,]+)""")
            // Pattern B: Kopi Susu Aren x 2 @ 18000
            val itemPatternB = Regex("""(?i)^[-*]?\s*([A-Za-z0-9\s()_-]+?)\s*x\s*(\d+)\s*[@=]?\s*(?:rp\.?)?\s*([\d.,]+)""")

            itemPatternA.find(line)?.let { match ->
                val qty = match.groupValues[1].toIntOrNull() ?: 1
                val name = match.groupValues[2].trim()
                val price = parseCurrencyDouble(match.groupValues[3])
                if (name.isNotBlank() && !isMetadataLabel(name)) {
                    items.add(OrderItem(id = "item_${System.currentTimeMillis()}_${items.size}", productId = "prod_${items.size + 1}", productName = name, quantity = qty, unitPrice = price))
                }
            } ?: itemPatternB.find(line)?.let { match ->
                val name = match.groupValues[1].trim()
                val qty = match.groupValues[2].toIntOrNull() ?: 1
                val price = parseCurrencyDouble(match.groupValues[3])
                if (name.isNotBlank() && !isMetadataLabel(name)) {
                    items.add(OrderItem(id = "item_${System.currentTimeMillis()}_${items.size}", productId = "prod_${items.size + 1}", productName = name, quantity = qty, unitPrice = price))
                }
            }
        }

        if (items.isEmpty()) {
            items.add(
                OrderItem(
                    id = "item_${System.currentTimeMillis()}_0",
                    productId = "prod_1",
                    productName = "Pesanan Chat",
                    quantity = 1,
                    unitPrice = 25000.0
                )
            )
        }

        val subtotal = items.sumOf { it.totalPrice }
        val totalAmount = maxOf(0.0, subtotal + shippingFee - discount)

        return Invoice(
            id = "inv_${UUID.randomUUID().toString().take(8)}",
            invoiceNumber = generateInvoiceNumber(),
            customerName = customerName,
            customerPhone = customerPhone,
            items = items,
            subtotal = subtotal,
            discount = discount,
            tax = 0.0,
            totalAmount = totalAmount,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            createdAt = System.currentTimeMillis(),
            shippingInfo = ShippingInfo(
                recipientName = customerName,
                phone = customerPhone ?: "",
                address = address ?: "Alamat Belum Diisi",
                courier = courier,
                shippingFee = shippingFee
            ),
            notes = notes
        )
    }

    private fun isMetadataLabel(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("nama") || lower.contains("alamat") || lower.contains("hp") ||
                lower.contains("ongkir") || lower.contains("diskon") || lower.contains("total") ||
                lower.contains("metode") || lower.contains("ekspedisi")
    }

    private fun cleanNumberString(raw: String): String {
        return raw.replace(Regex("""[^\d+]"""), "")
    }

    private fun parseCurrencyDouble(raw: String): Double {
        val clean = raw.replace(".", "").replace(",", "")
        return clean.toDoubleOrNull() ?: 0.0
    }

    private fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val randomNum = (100..999).random()
        return "INV-$dateStr-$randomNum"
    }

    suspend fun testApiKey(apiKey: String): Result<Boolean> {
        return runCatching {
            val trimmedKey = apiKey.trim()
            if (trimmedKey.isBlank()) {
                return Result.success(value = false)
            }
            val generativeModel = GenerativeModel(
                modelName = MODEL_TEXT,
                apiKey = trimmedKey
            )
            val response = generativeModel.generateContent("Hello! Reply with 'OK' if you can read this.")
            val responseText = response.text
            !responseText.isNullOrBlank()
        }
    }
}
