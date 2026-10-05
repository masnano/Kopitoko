package com.android.kopitoko.data

import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClipboardObserver(context: Context? = null) {

    private val clipboardManager: ClipboardManager? =
        context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    fun getClipboardText(): String? {
        return try {
            if (clipboardManager?.hasPrimaryClip() == true) {
                val item = clipboardManager.primaryClip?.getItemAt(0)
                item?.text?.toString()?.trim()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isOrderChatText(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val lower = text.lowercase()

        val orderKeywords = listOf(
            "pesan", "order", "nama", "alamat", "no hp", "telp", "wa",
            "ongkir", "total", "transfer", "qris", "expedisi", "ekspedisi",
            "kurir", "gosend", "grab", "jne", "j&t", "sicepat", "anteraja",
            "pembayaran", "lunas", "qty", "harga", "rek"
        )

        var keywordMatches = 0
        for (kw in orderKeywords) {
            if (lower.contains(kw)) {
                keywordMatches++
            }
        }

        val hasPricePattern = Regex("""(?i)(rp\.?\s*[\d.,]+|@\s*[\d.,]+|\d+\s*x|\d+\s*pcs)""").containsMatchIn(text)

        return (keywordMatches >= 2) || (keywordMatches >= 1 && hasPricePattern)
    }

    fun observeClipboardText(): Flow<String?> = callbackFlow {
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            trySend(getClipboardText())
        }

        trySend(getClipboardText())

        try {
            clipboardManager?.addPrimaryClipChangedListener(listener)
        } catch (_: Exception) { }

        awaitClose {
            try {
                clipboardManager?.removePrimaryClipChangedListener(listener)
            } catch (_: Exception) { }
        }
    }
}
