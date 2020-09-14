package cz.ackee.localizer.plugin.core

import com.squareup.moshi.Moshi
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class LocalizationsFetcher {

    companion object {
        private const val SHEETS_BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets"
    }

    private val moshi = Moshi.Builder().build()
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    fun fetch(configuration: FetchConfiguration) {
        val googleSheetsResponse = getGoogleSheetsResponse(configuration)
        if (googleSheetsResponse != null) {
            processGoogleSheetsResponse(googleSheetsResponse, configuration)
        }
    }

    private fun getGoogleSheetsResponse(configuration: FetchConfiguration): GoogleSheetResponse? {
        val url = buildUrl(configuration)
        val request = buildRequest(url)
        val response = okHttpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            response.body?.string()?.let { responseString ->
                moshi.adapter(GoogleSheetResponse::class.java).fromJson(responseString)
            } ?: throw IllegalArgumentException("Google Sheets response in invalid format")
        } else {
            throw IOException("${response.code} ${response.body?.string()}")
        }
    }

    private fun buildRequest(url: HttpUrl): Request {
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    private fun buildUrl(configuration: FetchConfiguration): HttpUrl {
        return SHEETS_BASE_URL.toHttpUrl()
            .newBuilder()
            .addEncodedPathSegment(configuration.sheetId)
            .addEncodedPathSegment("values")
            .addEncodedPathSegment(configuration.listName)
            .addEncodedQueryParameter("key", configuration.apiKey)
            .build()
    }

    private fun processGoogleSheetsResponse(googleSheetResponse: GoogleSheetResponse, configuration: FetchConfiguration) {
        val xmlGenerator = XmlGenerator(configuration.resourcesFolderPath, configuration.defaultLanguage)
        val localization = Localization.fromGoogleResponse(googleSheetResponse)
        xmlGenerator.createResourcesForLocalization(localization)
    }
}

data class FetchConfiguration(
    val sheetId: String,
    val listName: String,
    val apiKey: String,
    val defaultLanguage: String,
    val resourcesFolderPath: String
)