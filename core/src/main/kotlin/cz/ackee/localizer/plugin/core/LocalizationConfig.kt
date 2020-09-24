package cz.ackee.localizer.plugin.core

data class LocalizationConfig(
    val sheetId: String = "",
    val listName: String = "",
    val apiKey: String = "",
    val languageMapping: Map<String, String> = emptyMap(),
    val defaultLanguage: String = "",
    val resourcesFolderPath: String = ""
)