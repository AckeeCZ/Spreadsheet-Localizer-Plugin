package cz.ackee.localizer.plugin.core.auth

import com.google.auth.oauth2.GoogleCredentials
import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import java.io.File
import java.io.FileInputStream

interface CredentialsService {

    fun getCredentials(configuration: LocalizationConfig): Credentials
}

class CredentialsServiceImpl : CredentialsService {

    override fun getCredentials(configuration: LocalizationConfig): Credentials {
        return if (configuration.serviceAccount.isNotBlank()) {
            val file = File(configuration.serviceAccount)
            if (!file.exists()) {
                throw NoSuchFileException(file, null, "Service account file doesn't exist in provided location")
            }

            val googleCredentials = GoogleCredentials.fromStream(FileInputStream(configuration.serviceAccount))
                .createScoped(listOf(GOOGLE_AUTH_SPREADSHEET_SCOPE))
            Credentials.AccessToken(googleCredentials.accessToken.tokenValue)
        } else if (configuration.apiKey.isNotBlank()) {
            Credentials.ApiKey(configuration.apiKey)
        } else {
            throw IllegalArgumentException("Either api key or service account must be provided")
        }
    }

    companion object {

        const val GOOGLE_AUTH_SPREADSHEET_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly"
    }
}
