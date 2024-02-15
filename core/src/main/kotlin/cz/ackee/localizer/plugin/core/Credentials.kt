package cz.ackee.localizer.plugin.core

sealed interface Credentials {

    data class ApiKey(
        val value: String
    ) : Credentials
}
