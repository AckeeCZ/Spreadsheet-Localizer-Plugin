package cz.ackee.localizer.plugin.core.auth

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import cz.ackee.localizer.plugin.core.util.FileUtils
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test

class CredentialsServiceImplTest {

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

        val credentials = underTest.getCredentials(CONFIG_DIR_PATH, configuration)

        credentials shouldBeEqualTo Credentials.ApiKey(expectedApiKey)
    }

    @Test
    fun `Get service account credentials successfully`() {
        val configPath = CONFIG_DIR_PATH
        val pathToServiceAccount = "${configPath}/service-account.json".also {
            FileUtils.createTmpFile(it)
        }
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = pathToServiceAccount)

        val credentials = underTest.getCredentials(configPath, configuration)

        credentials shouldBeEqualTo Credentials.AccessToken(ACCESS_TOKEN_VALUE)
    }

    @Test
    fun `Error when service account path doesn't exist`() {
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = "path/that/does/not/exist.json")

        invoking {
            underTest.getCredentials(CONFIG_DIR_PATH, configuration)
        } shouldThrow NoSuchFileException::class
    }

    @Test
    fun `Error when api key nor service account specified`() {
        val configuration = LocalizationConfig(apiKey = "", serviceAccountPath = "")

        invoking {
            underTest.getCredentials(CONFIG_DIR_PATH, configuration)
        } shouldThrow IllegalArgumentException::class
    }

    companion object {

        private const val CONFIG_DIR_PATH = "test"
        private const val ACCESS_TOKEN_VALUE = "access_token_123"
    }
}
