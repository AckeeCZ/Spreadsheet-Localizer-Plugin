package cz.ackee.localizer.plugin.core.auth

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CredentialsServiceImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val googleAuthService = object : GoogleAuthService {
        override fun getAccessToken(serviceAccountPath: String, scopes: List<String>): GoogleAuthService.TokenValue {
            return GoogleAuthService.TokenValue(ACCESS_TOKEN_VALUE)
        }
    }

    private val underTest = CredentialsServiceImpl(
        googleAuthService = googleAuthService
    )

    @Test
    fun `Get api key credentials successfully`() {
        val expectedApiKey = "value"
        val configuration = LocalizationConfig(apiKey = expectedApiKey, serviceAccountPath = "")

        val credentials = underTest.getCredentials(CONFIG_DIR, configuration)

        credentials shouldBeEqualTo Credentials.ApiKey(expectedApiKey)
    }

    @Test
    fun `Get service account credentials successfully`() {
        val configPath = CONFIG_DIR
        val pathToServiceAccount = File(
            temporaryFolder.newFolder(configPath),
            "service-account.json",
        ).also { it.createNewFile() }
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = pathToServiceAccount.absolutePath)

        val credentials = underTest.getCredentials(configPath, configuration)

        credentials shouldBeEqualTo Credentials.AccessToken(ACCESS_TOKEN_VALUE)
    }

    @Test
    fun `Error when service account path doesn't exist`() {
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = "path/that/does/not/exist.json")

        invoking {
            underTest.getCredentials(CONFIG_DIR, configuration)
        } shouldThrow NoSuchFileException::class
    }

    @Test
    fun `Error when api key nor service account specified`() {
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = "")

        invoking {
            underTest.getCredentials(CONFIG_DIR, configuration)
        } shouldThrow IllegalArgumentException::class
    }

    companion object {

        private const val CONFIG_DIR = "test"
        private const val ACCESS_TOKEN_VALUE = "access_token_123"
    }
}
