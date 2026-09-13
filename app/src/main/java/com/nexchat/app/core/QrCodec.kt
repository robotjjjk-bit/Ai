package com.nexchat.app.core

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

object QrCodec {
    fun encodeProviderJson(json: String): Bitmap {
        val bits = QRCodeWriter().encode(json, BarcodeFormat.QR_CODE, 640, 640)
        val bmp = Bitmap.createBitmap(640, 640, Bitmap.Config.RGB_565)
        for (x in 0 until 640) for (y in 0 until 640) bmp.setPixel(x, y, if (bits.get(x, y)) 0xFF111111.toInt() else 0xFFFFFFFF.toInt())
        return bmp
    }
    fun providerToJson(name: String, baseUrl: String, kind: String, models: String, headers: String): String =
        JSONObject().put("v", 1).put("name", name).put("baseUrl", baseUrl).put("kind", kind)
            .put("models", models).put("headers", JSONObject(headers)).toString()
}
