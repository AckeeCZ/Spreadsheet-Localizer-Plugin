package cz.ackee.localizer.plugin.core.localization

import cz.ackee.localizer.plugin.core.auth.Credentials
import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

interface LocalizationsRepository {

    fun getLocalization(configuration: LocalizationConfig, credentials: Credentials): Localization
}

class LocalizationsRepositoryImpl(
    private val localizationsRequestBuilder: LocalizationsRequestBuilder = LocalizationsRequestBuilder()
) : LocalizationsRepository {

    private val json: Json = Json { ignoreUnknownKeys = true }
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    override fun getLocalization(configuration: LocalizationConfig, credentials: Credentials): Localization {
        val request = localizationsRequestBuilder.build(configuration, credentials)

        val googleSheetResponse = getGoogleSheetsResponse(request)

        return Localization.fromGoogleResponse(googleSheetResponse, configuration)
    }

    private fun getGoogleSheetsResponse(request: Request): GoogleSheetResponse {
        val response = okHttpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            response.body?.string()?.let { responseString ->
                json.decodeFromString<GoogleSheetResponse>(responseString)
            } ?: throw IllegalArgumentException("Google Sheets response in invalid format")
        } else {
            throw IOException("${response.code} ${response.body?.string()}")
        }
    }
}
