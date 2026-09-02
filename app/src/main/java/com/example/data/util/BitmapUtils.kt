package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.max

object BitmapUtils {

    /**
     * Safely decodes a Uri into a mutable software ARGB_8888 Bitmap,
     * scaled down to a maximum dimension to prevent memory errors and ensure
     * compatibility with ML Kit and Gemini Vision API compression.
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1280): Bitmap? {
        return try {
            // First pass: decode bounds
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null
            }

            // Calculate sample size
            val maxOriginal = max(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxOriginal / sampleSize > maxDimension * 1.5) {
                sampleSize *= 2
            }

            // Second pass: decode sampled bitmap as software ARGB_8888
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (sampledBitmap == null) return null

            // Check Exif rotation
            val rotationDegrees = getExifOrientationDegrees(context, uri)
            val finalBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(
                    sampledBitmap,
                    0,
                    0,
                    sampledBitmap.width,
                    sampledBitmap.height,
                    matrix,
                    true
                )
            } else {
                sampledBitmap
            }

            // Ensure SOFTWARE bitmap config (not HARDWARE)
            if (finalBitmap.config != Bitmap.Config.ARGB_8888) {
                finalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                finalBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Rescales a bitmap to a maximum bounding box while preserving aspect ratio.
     */
    fun scaleBitmap(bitmap: Bitmap, maxDimension: Int = 1280): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)
        if (maxSide <= maxDimension) {
            return if (bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                bitmap
            }
        }

        val scale = maxDimension.toFloat() / maxSide
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getExifOrientationDegrees(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exifInterface = ExifInterface(inputStream)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
