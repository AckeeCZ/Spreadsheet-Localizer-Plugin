package cz.ackee.localizer.plugin.core.configuration

data class LocalizationConfig(
    val fileId: String = "",
    val sheetName: String = "",
    val apiKey: String = "",
    val serviceAccountPath: String = "",
    val languageMapping: Map<String, String?> = emptyMap(),
    val resourcesFolderPath: String = ""
)
