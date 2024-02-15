package cz.ackee.localizer.plugin.core

import com.squareup.moshi.Moshi
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class LocalizationsFetcher {

    private val moshi = Moshi.Builder().build()
    private val okHttpClient = OkHttpClient.Builder().build()

    fun fetch(configuration: LocalizationConfig, credentials: Credentials): GoogleSheetResponse {
        val request = buildRequest(configuration)

        val requestWithCredentials = request.addCredentialsToRequest(credentials)

        return getGoogleSheetsResponse(requestWithCredentials)
    }

    private fun buildRequest(configuration: LocalizationConfig): Request {
        val url = buildUrl(configuration)
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
            .build()
    }

    private fun getGoogleSheetsResponse(request: Request): GoogleSheetResponse {
        val response = okHttpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            response.body?.string()?.let { responseString ->
                moshi.adapter(GoogleSheetResponse::class.java).fromJson(responseString)
            } ?: throw IllegalArgumentException("Google Sheets response in invalid format")
        } else {
            throw IOException("${response.code} ${response.body?.string()}")
        }
    }

    private fun Request.addCredentialsToRequest(credentials: Credentials): Request {
        return when (credentials) {
            is Credentials.ApiKey -> {
                newBuilder()
                    .url(
                        url.newBuilder()
                            .addEncodedQueryParameter("key", credentials.value)
                            .build()
                    )
                    .build()
            }
        }
    }

    companion object {

        private const val SHEETS_BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets"
    }
}
