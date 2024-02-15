package cz.ackee.localizer.plugin.core

import com.squareup.moshi.Moshi
import java.io.File

class ConfigurationParser {

    private val moshi = Moshi.Builder().build()

    fun parse(configPath: String): LocalizationConfig {
        val file = File(configPath)

        if (!file.exists()) {
            throw NoSuchFileException(file, null, "Config file doesn't exist in provided location")
        }

        val moshiAdapter = moshi.adapter(LocalizationConfig::class.java)
        return moshiAdapter.fromJson(file.readText()) ?: throw IllegalArgumentException("Can't parse config file")
    }
}
