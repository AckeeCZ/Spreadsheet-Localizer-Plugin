package cz.ackee.localizer.plugin.core.configuration

import com.squareup.moshi.Moshi
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test
import java.io.File

class ConfigurationParserTest {

    private val moshi: Moshi = Moshi.Builder().build()

    private val underTest = ConfigurationParser(moshi)

    @Test
    fun `Parse configuration file successfully`() {
        val config = testConfiguration
        val path = "test/test-configuration.json"
        createTestConfigurationFile(config, path)

        val parsedConfig = underTest.parse(path)

        parsedConfig shouldBeEqualTo testConfiguration
    }

    @Test
    fun `Configuration file doesn't exist`() {
        invoking {
            underTest.parse("path/that/does/not/exist.json")
        } shouldThrow NoSuchFileException::class
    }

    private fun createTestConfigurationFile(
        configuration: LocalizationConfig,
        path: String
    ) {
        val file = File(path)

        val moshiAdapter = moshi.adapter(LocalizationConfig::class.java)
        val configSerialized = moshiAdapter.toJson(configuration)

        file.writeText(configSerialized.trimIndent())
    }

    companion object {

        private val testConfiguration = LocalizationConfig(
            fileId = "1122334455",
            sheetName = "Sheet 1",
            apiKey = "AIza112233",
            languageMapping = mapOf("CZ" to "cs", "FR-FR" to "fr-rFR"),
            resourcesFolderPath = "libraries/translations/src/main/res"
        )
    }
}
