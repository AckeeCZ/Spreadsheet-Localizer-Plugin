package cz.ackee.localizer.plugin.core

import com.squareup.moshi.Moshi
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class LocalizationsFetcher {

    companion object {
        private const val SHEETS_BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets"
    }

    private val moshi = Moshi.Builder().build()
    private val okHttpClient = OkHttpClient.Builder().build()

    fun fetch(configPath: String) {
        val config = parseConfigFile(configPath)
        val googleSheetsResponse = getGoogleSheetsResponse(config)
        if (googleSheetsResponse != null) {
            processGoogleSheetsResponse(googleSheetsResponse, config)
        }
    }

    private fun parseConfigFile(path: String): LocalizationConfig {
        val file = File(path)

        if (!file.exists()) {
            throw NoSuchFileException(file, null, "Config file doesn't exist in provided location")
        }

        val moshiAdapter = moshi.adapter(LocalizationConfig::class.java)
        val fetchConfiguration = moshiAdapter.fromJson(file.readText())!!

        return fetchConfiguration
    }

    private fun getGoogleSheetsResponse(configuration: LocalizationConfig): GoogleSheetResponse? {
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

    private fun buildUrl(configuration: LocalizationConfig): HttpUrl {
        return SHEETS_BASE_URL.toHttpUrl()
            .newBuilder()
            .addEncodedPathSegment(configuration.fileId)
            .addEncodedPathSegment("values")
            .addEncodedPathSegment(configuration.sheetName)
            .addEncodedQueryParameter("key", configuration.apiKey)
            .build()
    }

    private fun processGoogleSheetsResponse(googleSheetResponse: GoogleSheetResponse, configuration: LocalizationConfig) {
        val xmlGenerator = XmlGenerator(configuration.resFolderPath)
        val localization = Localization.fromGoogleResponse(googleSheetResponse, configuration)
        xmlGenerator.createResourcesForLocalization(localization)
    }
}
