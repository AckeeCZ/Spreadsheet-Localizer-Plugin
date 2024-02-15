package cz.ackee.localizer.plugin.core

class CredentialsService {

    fun getCredentials(configuration: LocalizationConfig): Credentials {
        return if (configuration.apiKey.isNotBlank()) {
            Credentials.ApiKey(configuration.apiKey)
        } else {
            throw IllegalArgumentException("Api key must be provided")
        }
    }
}
