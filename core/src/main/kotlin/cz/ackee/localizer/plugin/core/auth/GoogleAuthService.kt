package cz.ackee.localizer.plugin.core.auth

import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

interface GoogleAuthService {

    fun getAccessToken(serviceAccountPath: String, scopes: List<String>): TokenValue

    data class TokenValue(
        val value: String
    )
}

class GoogleAuthServiceImpl : GoogleAuthService {

    override fun getAccessToken(serviceAccountPath: String, scopes: List<String>): GoogleAuthService.TokenValue {
        val inputStream = FileInputStream(serviceAccountPath)
        val googleCredentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(scopes)
        googleCredentials.refresh()

        val token = googleCredentials.accessToken.tokenValue
        return GoogleAuthService.TokenValue(token)
    }
}
