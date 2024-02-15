package cz.ackee.localizer.plugin.core.localization

import cz.ackee.localizer.plugin.core.auth.Credentials
import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

class LocalizationsRequestBuilder {

    fun build(configuration: LocalizationConfig, credentials: Credentials): Request {
        val request = buildRequest(configuration)

        return request.addCredentialsToRequest(credentials)
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
