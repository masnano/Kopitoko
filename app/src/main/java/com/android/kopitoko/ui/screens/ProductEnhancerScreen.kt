package com.android.kopitoko.ui.screens

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.kopitoko.domain.model.StudioBackgroundTheme
import com.android.kopitoko.domain.model.StudioBackgroundType
import com.android.kopitoko.util.ShareUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEnhancerScreen(
    inputBitmap: Bitmap?,
    onOpenCamera: () -> Unit,
    onUseForCaption: (Bitmap) -> Unit,
    onAddToProducts: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sourceBitmap = remember(inputBitmap) {
        inputBitmap ?: createDefaultProductBitmap()
    }

    var selectedTheme by remember { mutableStateOf(StudioBackgroundTheme.ALL_THEMES.first()) }
    var showBadge by remember { mutableStateOf(true) }
    var showShadow by remember { mutableStateOf(true) }
    var brightnessBoost by remember { mutableFloatStateOf(1.05f) }

    val enhancedBitmap = remember(sourceBitmap, selectedTheme, showBadge, showShadow, brightnessBoost) {
        createEnhancedBitmap(
            original = sourceBitmap,
            theme = selectedTheme,
            showBadge = showBadge,
            showShadow = showShadow,
            brightnessBoost = brightnessBoost
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Product Studio Enhancer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Apply studio backdrops, lighting, and Kopitoko branding overlays.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            // Enhanced Preview Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = enhancedBitmap.asImageBitmap(),
                            contentDescription = "Enhanced Product Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenCamera,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (inputBitmap == null) "Capture / Select Photo" else "Retake / Pick New Photo")
                    }
                }
            }
        }

        item {
            // Theme Selector Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ColorLens,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Studio Backdrop Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(StudioBackgroundTheme.ALL_THEMES, key = { it.type.name }) { theme ->
                        val isSelected = selectedTheme.type == theme.type
                        Card(
                            onClick = { selectedTheme = theme },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(theme.primaryColor)
                                            .border(1.dp, theme.accentColor, CircleShape)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = theme.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Enhancement Options Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Studio Effects & Overlays",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Kopitoko Catalog Badge")
                        }
                        Switch(
                            checked = showBadge,
                            onCheckedChange = { showBadge = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Studio Floor Drop Shadow")
                        }
                        Switch(
                            checked = showShadow,
                            onCheckedChange = { showShadow = it }
                        )
                    }

                    Text(
                        text = "Lighting Boost",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            1.0f to "Natural",
                            1.08f to "Bright Studio",
                            1.15f to "High Vibrance"
                        ).forEach { (boost, label) ->
                            FilterChip(
                                selected = brightnessBoost == boost,
                                onClick = { brightnessBoost = boost },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        item {
            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onUseForCaption(enhancedBitmap) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate AI Caption with Photo")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onAddToProducts(enhancedBitmap) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Product")
                    }

                    OutlinedButton(
                        onClick = {
                            ShareUtil.shareImageWithCaption(
                                context = context,
                                bitmap = enhancedBitmap,
                                caption = "Enhanced product photo via Kopitoko Studio"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Photo")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun createEnhancedBitmap(
    original: Bitmap,
    theme: StudioBackgroundTheme,
    showBadge: Boolean = true,
    showShadow: Boolean = true,
    brightnessBoost: Float = 1.0f
): Bitmap {
    val width = 1080
    val height = 1080
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    // 1. Draw Background Gradient
    val bgPaint = Paint().apply { isAntiAlias = true }
    val gradient = LinearGradient(
        0f, 0f, width.toFloat(), height.toFloat(),
        theme.primaryColor.toArgb(),
        theme.secondaryColor.toArgb(),
        Shader.TileMode.CLAMP
    )
    bgPaint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Background motif details
    when (theme.type) {
        StudioBackgroundType.WOOD_TABLE_AESTHETIC -> {
            val linePaint = Paint().apply {
                color = android.graphics.Color.argb(35, 255, 255, 255)
                strokeWidth = 5f
                isAntiAlias = true
            }
            for (i in 0..height step 70) {
                canvas.drawLine(0f, i.toFloat(), width.toFloat(), i + 30f, linePaint)
            }
        }
        StudioBackgroundType.WARM_COFFEE_SHOP -> {
            val glowPaint = Paint().apply {
                shader = RadialGradient(
                    width / 2f, height / 3f, 550f,
                    theme.accentColor.toArgb(),
                    android.graphics.Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
        }
        StudioBackgroundType.MINIMALIST_MARBLE -> {
            val marblePaint = Paint().apply {
                color = theme.accentColor.copy(alpha = 0.25f).toArgb()
                strokeWidth = 4f
                isAntiAlias = true
            }
            canvas.drawLine(120f, 0f, width - 120f, height.toFloat(), marblePaint)
            canvas.drawLine(width - 180f, 0f, 180f, height.toFloat(), marblePaint)
        }
        StudioBackgroundType.CLEAN_STUDIO_WHITE -> {
            val floorGradient = LinearGradient(
                0f, height * 0.65f, 0f, height.toFloat(),
                android.graphics.Color.argb(0, 0, 0, 0),
                android.graphics.Color.argb(30, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
            val floorPaint = Paint().apply { shader = floorGradient; isAntiAlias = true }
            canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), floorPaint)
        }
        StudioBackgroundType.SOFT_PASTEL_PINK -> {
            val circlePaint = Paint().apply {
                color = theme.accentColor.copy(alpha = 0.18f).toArgb()
                isAntiAlias = true
            }
            canvas.drawCircle(width / 2f, height / 2f, 440f, circlePaint)
        }
    }

    // 2. Draw Shadow under product photo
    val margin = 120f
    val photoRect = RectF(margin, margin, width - margin, height - margin)

    if (showShadow) {
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(90, 0, 0, 0)
            maskFilter = BlurMaskFilter(45f, BlurMaskFilter.Blur.NORMAL)
            isAntiAlias = true
        }
        val shadowRect = RectF(margin + 12f, margin + 28f, width - margin + 12f, height - margin + 28f)
        canvas.drawRoundRect(shadowRect, 36f, 36f, shadowPaint)
    }

    // 3. Draw Product Image with rounded corners and brightness filter
    val bitmapPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        if (brightnessBoost != 1.0f) {
            val colorMatrix = ColorMatrix()
            colorMatrix.setScale(brightnessBoost, brightnessBoost, brightnessBoost, 1.0f)
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
    }

    val clipPath = Path().apply {
        addRoundRect(photoRect, 36f, 36f, Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(clipPath)
    canvas.drawBitmap(original, null, photoRect, bitmapPaint)
    canvas.restore()

    // 4. Draw Studio Border Frame
    val borderPaint = Paint().apply {
        color = theme.accentColor.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    canvas.drawRoundRect(photoRect, 36f, 36f, borderPaint)

    // 5. Draw Kopitoko Badge if enabled
    if (showBadge) {
        val badgeBgPaint = Paint().apply {
            color = theme.accentColor.toArgb()
            isAntiAlias = true
        }
        val badgeRect = RectF(margin + 20f, margin + 20f, margin + 360f, margin + 85f)
        canvas.drawRoundRect(badgeRect, 22f, 22f, badgeBgPaint)

        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("☕ KOPITOKO CATALOG", margin + 35f, margin + 62f, textPaint)
    }

    return output
}

fun createDefaultProductBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgPaint = Paint().apply {
        color = android.graphics.Color.rgb(240, 235, 225)
        isAntiAlias = true
    }
    canvas.drawRect(0f, 0f, 600f, 600f, bgPaint)

    val cupPaint = Paint().apply {
        color = android.graphics.Color.rgb(110, 60, 30)
        isAntiAlias = true
    }
    canvas.drawCircle(300f, 300f, 180f, cupPaint)

    val innerPaint = Paint().apply {
        color = android.graphics.Color.rgb(180, 120, 70)
        isAntiAlias = true
    }
    canvas.drawCircle(300f, 300f, 150f, innerPaint)

    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 32f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText("Kopitoko Product", 300f, 310f, textPaint)
    return bitmap
}
