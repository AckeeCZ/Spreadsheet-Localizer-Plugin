package cz.ackee.localizer.plugin.core

data class LocalizationConfig(
    val fileId: String = "",
    val sheetName: String = "",
    val apiKey: String = "",
    val languageMapping: Map<String, String?> = emptyMap(),
    val resFolderPath: String = ""
)