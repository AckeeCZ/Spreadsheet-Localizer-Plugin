package cz.ackee.localizer.plugin.core.configuration

import kotlinx.serialization.json.Json
import java.io.File

interface ConfigurationParser {

    fun parse(configPath: String): LocalizationConfig
}

class ConfigurationParserImpl : ConfigurationParser {

    private val json: Json = Json

    override fun parse(configPath: String): LocalizationConfig {
        val file = File(configPath)
        if (!file.exists()) {
            throw NoSuchFileException(file, null, "Config file doesn't exist in provided location")
        }
        return json.decodeFromString<LocalizationConfig>(file.readText())
    }
}
