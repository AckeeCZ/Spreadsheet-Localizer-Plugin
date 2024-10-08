package cz.ackee.localizer.plugin.core.configuration

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConfigurationParserImplTest {

    private val underTest = ConfigurationParserImpl()

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `Parse configuration file successfully`() {
        val expectedConfig = LocalizationConfig(
            fileId = "1122334455",
            sheetName = "Sheet 1",
            apiKey = "AIza112233",
            languageMapping = mapOf("CZ" to "cs", "FR-FR" to "fr-rFR"),
            resourcesFolderPath = "libraries/translations/src/main/res",
            supportEmptyStrings = true,
        )
        val file = createTestConfigurationFileFrom(expectedConfig)

        val actualConfig = underTest.parse(file.absolutePath)

        actualConfig shouldBeEqualTo expectedConfig
    }

    private fun createTestConfigurationFileFrom(
        config: LocalizationConfig,
        includeSupportEmptyStrings: Boolean = true,
    ): File {
        val serializedConfig = buildJsonObject {
            put("fileId", config.fileId)
            put("sheetName", config.sheetName)
            put("apiKey", config.apiKey)
            putJsonObject("languageMapping") {
                config.languageMapping.forEach { (key, value) ->
                    put(key, value)
                }
            }
            put("resourcesFolderPath", config.resourcesFolderPath)
            if (includeSupportEmptyStrings) {
                put("supportEmptyStrings", config.supportEmptyStrings)
            }
        }.toString()
        return temporaryFolder.newFile("config.json").also { it.writeText(serializedConfig.trimIndent()) }
    }

    @Test
    fun `Configuration file doesn't exist`() {
        invoking {
            underTest.parse("path/that/does/not/exist.json")
        } shouldThrow NoSuchFileException::class
    }

    @Test
    fun `Default to supportEmptyStrings=false if the attribute is missing`() {
        val file = createTestConfigurationFileFrom(LocalizationConfig(), includeSupportEmptyStrings = false)

        val actualConfig = underTest.parse(file.absolutePath)

        actualConfig.supportEmptyStrings shouldBeEqualTo false
    }
}
