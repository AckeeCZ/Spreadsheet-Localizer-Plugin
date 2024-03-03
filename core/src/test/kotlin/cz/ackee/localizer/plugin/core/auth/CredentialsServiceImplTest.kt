package cz.ackee.localizer.plugin.core.auth

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test

class CredentialsServiceImplTest {

    private val underTest = CredentialsServiceImpl()

    @Test
    fun `Get api key credentials successfully`() {
        val expectedApiKey = "value"
        val configuration = LocalizationConfig(apiKey = expectedApiKey)

        val credentials = underTest.getCredentials(configuration)

        credentials shouldBeEqualTo Credentials.ApiKey(expectedApiKey)
    }

    @Test
    fun `Error when api key not specified`() {
        val configuration = LocalizationConfig(apiKey = "")

        invoking {
            underTest.getCredentials(configuration)
        } shouldThrow IllegalArgumentException::class
    }
}
