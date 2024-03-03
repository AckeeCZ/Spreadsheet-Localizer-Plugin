package cz.ackee.localizer.plugin.core.auth

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig

interface CredentialsService {

    fun getCredentials(configuration: LocalizationConfig): Credentials
}

class CredentialsServiceImpl : CredentialsService {

    override fun getCredentials(configuration: LocalizationConfig): Credentials {
        return if (configuration.apiKey.isNotBlank()) {
            Credentials.ApiKey(configuration.apiKey)
        } else {
            throw IllegalArgumentException("Api key must be provided")
        }
    }
}
