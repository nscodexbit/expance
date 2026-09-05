package com.yourname.expensetracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class ProductLookupResult(
    val barcode: String,
    val name: String,
    val brand: String? = null,
    val category: String? = null,
    val imageUrl: String? = null
)

@Singleton
class ProductLookupService @Inject constructor() {

    suspend fun lookupProduct(barcode: String): ProductLookupResult? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "ExpenseTrackerApp/2.0 (sknoyeem45@gmail.com)")

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                val status = root.optInt("status", 0)
                if (status == 1) {
                    val product = root.optJSONObject("product")
                    if (product != null) {
                        val name = product.optString("product_name").ifBlank {
                            product.optString("product_name_en")
                        }
                        val brand = product.optString("brands")
                        val category = product.optString("categories")
                        val image = product.optString("image_url")
                        return@withContext ProductLookupResult(
                            barcode = barcode,
                            name = if (name.isNotBlank()) name else "Product $barcode",
                            brand = brand.ifBlank { null },
                            category = category.ifBlank { null },
                            imageUrl = image.ifBlank { null }
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
