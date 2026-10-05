package com.android.kopitoko.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtil {
    /**
     * Copies plain text to the system clipboard and displays a Toast feedback notification.
     * @param context Context to access system services and show Toast.
     * @param label A label describing the copied content (e.g., "Invoice Text", "Shipping Label").
     * @param text The text content to be copied.
     * @param showToast Whether to display a Toast notification upon successful copy.
     * @return True if copying succeeded, false otherwise.
     */
    fun copyToClipboard(
        context: Context,
        label: String,
        text: String,
        showToast: Boolean = true
    ): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            if (showToast) {
                Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
