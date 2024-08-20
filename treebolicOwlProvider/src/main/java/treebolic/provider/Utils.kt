/*
 * Copyright (c) 2023. Bernard Bou
 */
package treebolic.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Image utilities
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Utils {

    private fun bytesToImage(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun roundCrop(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawARGB(0, 0, 0, 0)
        val rect = Rect(0, 0, w, h)

        val color = -0x7f7f80
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        canvas.drawCircle(h / 2f, h / 2f, h / 2f, paint) // canvas.drawRoundRect(rect, roundPx, roundPx, paint)

        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun scale(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, w, h, false)
    }

    fun bitmapToUrl(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return "data:image/png;base64,$imageBase64"
    }
}
