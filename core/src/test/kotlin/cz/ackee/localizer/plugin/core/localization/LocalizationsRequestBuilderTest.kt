package cz.ackee.localizer.plugin.core.localization

import cz.ackee.localizer.plugin.core.auth.Credentials
import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.io.File

class LocalizationsRequestBuilderTest {

    private val underTest = LocalizationsRequestBuilder()

    @Test
    fun `Build localizations api key request successfully`() {
        val fileId = "fileId1"
        val sheetName = "sheetName2"
        val apiKey = "apiKey123"
        val configuration = LocalizationConfig(fileId = fileId, sheetName = sheetName, apiKey = apiKey)
        val credentials = Credentials.ApiKey(apiKey)

        val request = underTest.build(configuration, credentials)

        request.url.toString() shouldBeEqualTo getExpectedUrl(fileId, sheetName, apiKey)
    }

    @Test
    fun `Build localizations service account request successfully`() {
        val fileId = "fileId3"
        val sheetName = "sheetName4"
        val accessToken = "accessToken123"

        val pathToServiceAccount = "test/service-account.json" // TODO refactor
        val file = File(pathToServiceAccount)
        file.createNewFile()
        file.deleteOnExit()

        val configuration = LocalizationConfig(fileId = fileId, sheetName = sheetName, serviceAccountPath = pathToServiceAccount)
        val credentials = Credentials.AccessToken(accessToken)

        val request = underTest.build(configuration, credentials)

        request.url.toString() shouldBeEqualTo getExpectedUrl(fileId, sheetName, apiKey = null)
        request.header(LocalizationsRequestBuilder.AUTH_HEADER) shouldBeEqualTo accessToken
    }

    private fun getExpectedUrl(fileId: String, sheetName: String, apiKey: String? = null): String {
        var url = "${LocalizationsRequestBuilder.SHEETS_BASE_URL}/$fileId/values/$sheetName"
        if (apiKey != null) {
            url += "?key=$apiKey"
        }
        return url
    }
}
