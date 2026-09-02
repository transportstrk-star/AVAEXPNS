package com.example.data.local

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocalOcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extracts text from an Android Bitmap using Google ML Kit on-device Text Recognition.
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    continuation.resume(fullText)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    continuation.resume("")
                }
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume("")
        }
    }
}
