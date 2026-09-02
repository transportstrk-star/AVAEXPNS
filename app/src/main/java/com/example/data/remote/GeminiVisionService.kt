package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.util.BitmapUtils
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiReceiptResult(
    val supplier: String? = null,
    val trn: String? = null,
    val invoiceNumber: String? = null,
    val date: String? = null,
    val description: String? = null,
    val totalAed: Double? = null,
    val vatAed: Double? = null,
    val netAed: Double? = null,
    val vatRate: Double? = null,
    val category: String? = null,
    val rawText: String? = null,
    val confidence: String? = null
)

class GeminiVisionService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeReceipt(bitmap: Bitmap): GeminiReceiptResult? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiVisionService", "Gemini API key not configured in .env, skipping vision request")
            return@withContext null
        }

        try {
            // Guarantee software ARGB_8888 bitmap & safe resolution for Gemini API
            val safeBitmap = BitmapUtils.scaleBitmap(bitmap, maxDimension = 1024)

            val outputStream = ByteArrayOutputStream()
            safeBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                You are an expert UAE Tax Invoice and VAT OCR reader.
                Analyze this receipt image and extract the following fields in strict JSON format:
                {
                   "supplier": "Company or store name (e.g., ADNOC Distribution, Lulu Hypermarket, Carrefour, Sharaf DG)",
                   "trn": "15-digit UAE Tax Registration Number / TRN if visible",
                   "invoiceNumber": "Invoice or Bill #",
                   "date": "Date in YYYY-MM-DD format",
                   "description": "Brief summary of purchased items (e.g., Super 98 Fuel 40L or Groceries)",
                   "totalAed": 0.00,
                   "vatAed": 0.00,
                   "netAed": 0.00,
                   "vatRate": 5.0,
                   "category": "One of: Fuel & Transport, Groceries & Supplies, Office & Electronics, Meals & Dining, Utilities & Telecom, Healthcare, General",
                   "rawText": "Full or key lines of text recognized from the receipt",
                   "confidence": "HIGH"
                }
                If any field is missing, compute total, vat, and net assuming standard UAE 5% VAT if one of them is present: Net = Total / 1.05, VAT = Total - Net. Return ONLY valid JSON.
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("GeminiVisionService", "API call returned ${response.code}: ${response.message}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val candidate = candidates.getJSONObject(0)
            val parts = candidate.optJSONObject("content")?.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            val textResponse = parts.getJSONObject(0).optString("text", "")
            if (textResponse.isBlank()) return@withContext null

            // Clean markdown code blocks if any
            val cleanJson = textResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsedJson = JSONObject(cleanJson)
            return@withContext GeminiReceiptResult(
                supplier = parsedJson.optString("supplier", null),
                trn = parsedJson.optString("trn", null),
                invoiceNumber = parsedJson.optString("invoiceNumber", null),
                date = parsedJson.optString("date", null),
                description = parsedJson.optString("description", null),
                totalAed = parsedJson.optDouble("totalAed", 0.0).takeIf { !it.isNaN() && it > 0 },
                vatAed = parsedJson.optDouble("vatAed", 0.0).takeIf { !it.isNaN() && it > 0 },
                netAed = parsedJson.optDouble("netAed", 0.0).takeIf { !it.isNaN() && it > 0 },
                vatRate = parsedJson.optDouble("vatRate", 5.0),
                category = parsedJson.optString("category", "General"),
                rawText = parsedJson.optString("rawText", ""),
                confidence = parsedJson.optString("confidence", "HIGH")
            )
        } catch (e: Exception) {
            Log.e("GeminiVisionService", "Exception during Gemini Vision call", e)
            return@withContext null
        }
    }
}
