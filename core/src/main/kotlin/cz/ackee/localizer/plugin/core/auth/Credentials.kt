package cz.ackee.localizer.plugin.core.auth

sealed interface Credentials {

    data class ApiKey(
        val value: String
    ) : Credentials
}
