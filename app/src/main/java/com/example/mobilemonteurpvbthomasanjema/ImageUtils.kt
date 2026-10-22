package com.example.mobilemonteurpvbthomasanjema

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.ImageView
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    /**
     * Laadt een verkleinde versie van een foto en draait hem goed (camera's zetten de
     * draairichting vaak alleen in de EXIF-gegevens). targetPx is de gewenste lange zijde.
     */
    fun loadBitmap(path: String, targetPx: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= targetPx) {
            sample *= 2
        }
        val bitmap = BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return null

        val degrees = try {
            when (
                ExifInterface(path).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            0f
        }
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Verkleint een foto die de camera net heeft gemaakt naar maximaal maxPx op de lange
     * zijde (JPEG, kwaliteit 85) en slaat hem op onder dezelfde naam. Een foto van 12 MP
     * is dan nog scherp genoeg om een defect te zien, maar niet meer 4 MB per bijlage.
     */
    fun shrinkPhoto(path: String, maxPx: Int = 2048): Boolean {
        var bitmap = loadBitmap(path, maxPx) ?: return false
        val longSide = maxOf(bitmap.width, bitmap.height)
        if (longSide > maxPx) {
            val scale = maxPx.toFloat() / longSide
            bitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        }
        return try {
            FileOutputStream(path).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}

/** Toont een foto in de ImageView, of een camera-icoon als het bestand ontbreekt. */
fun ImageView.showPhoto(path: String, targetPx: Int) {
    val bitmap = ImageUtils.loadBitmap(path, targetPx)
    if (bitmap != null) {
        imageTintList = null
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageBitmap(bitmap)
    } else {
        scaleType = ImageView.ScaleType.CENTER
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary))
        setImageResource(R.drawable.ic_camera)
    }
}
