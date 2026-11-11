package com.helpmom.mother.feature.family.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.helpmom.mother.feature.family.data.FamilyApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class InvitePartnerViewModel(private val familyApi: FamilyApi) : ViewModel() {

    var code: String by mutableStateOf("")
        private set
    var expiresAt: String by mutableStateOf("")
        private set
    var expiresCountdownText: String by mutableStateOf("")
        private set
    var partnerName: String? by mutableStateOf(null)
        private set
    var qrBitmap: Bitmap? by mutableStateOf(null)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    fun loadOrGenerateInvitation() {
        viewModelScope.launch {
            isLoading = true
            runCatching { familyApi.getFamily().body() }
                .onSuccess { family ->
                    partnerName = family?.members?.firstOrNull { it.role == 3 }?.fullName
                }

            runCatching { familyApi.createInvitation() }.onSuccess { response ->
                val invitation = response.body()
                if (response.isSuccessful && invitation != null) {
                    code = invitation.code
                    expiresAt = invitation.expiresAt
                    qrBitmap = generateQR(code)
                    startCountdown()
                }
            }
            isLoading = false
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            val expiry = runCatching { Instant.parse(expiresAt) }.getOrNull() ?: return@launch
            while (true) {
                val seconds = Duration.between(Instant.now(), expiry).seconds
                if (seconds <= 0) {
                    expiresCountdownText = "Expirado"
                    break
                } else {
                    val hours = seconds / 3600
                    val minutes = (seconds % 3600) / 60
                    expiresCountdownText = if (hours >= 1) {
                        "Expira en $hours h $minutes min"
                    } else {
                        val secs = seconds % 60
                        "Expira en ${String.format("%02d:%02d", minutes, secs)} min"
                    }
                }
                delay(1000)
            }
        }
    }

    private fun generateQR(textCode: String): Bitmap? {
        return runCatching {
            val size = 440
            val matrix = QRCodeWriter().encode(textCode, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val darkColor = android.graphics.Color.parseColor("#1A1C1E")
            val lightColor = android.graphics.Color.WHITE
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) darkColor else lightColor)
                }
            }
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = android.graphics.Color.parseColor("#4A6FA5")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val center = size / 2f
            val logoSize = 64f
            val rect = android.graphics.RectF(center - logoSize / 2, center - logoSize / 2, center + logoSize / 2, center + logoSize / 2)
            canvas.drawRoundRect(rect, 12f, 12f, paint)

            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val textY = center - ((textPaint.descent() + textPaint.ascent()) / 2)
            canvas.drawText("H", center, textY, textPaint)

            bitmap
        }.getOrNull()
    }
}
