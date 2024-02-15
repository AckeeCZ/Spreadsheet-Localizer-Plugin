package cz.ackee.localizer.plugin.core.auth

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import java.io.File

interface CredentialsService {

    fun getCredentials(configuration: LocalizationConfig): Credentials
}

class CredentialsServiceImpl(
    private val googleAuthService: GoogleAuthService = GoogleAuthServiceImpl()
) : CredentialsService {

    override fun getCredentials(configuration: LocalizationConfig): Credentials {
        return if (configuration.serviceAccountPath.isNotBlank()) {
            val file = File(configuration.serviceAccountPath)
            if (!file.exists()) {
                throw NoSuchFileException(file, null, "Service account file doesn't exist in provided location")
            }
            val accessToken = googleAuthService.getAccessToken(configuration.serviceAccountPath, listOf(GOOGLE_AUTH_SPREADSHEET_SCOPE))
            Credentials.AccessToken(accessToken.value)
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
