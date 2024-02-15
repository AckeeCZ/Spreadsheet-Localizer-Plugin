package cz.ackee.localizer.plugin.core

import java.io.File

class GoogleSheetsResponseProcessor {

    fun processGoogleSheetsResponse(configPath: String, googleSheetResponse: GoogleSheetResponse, configuration: LocalizationConfig) {
        val xmlGenerator = XmlGenerator(File(File(configPath).parent, configuration.resourcesFolderPath))
        val localization = Localization.fromGoogleResponse(googleSheetResponse, configuration)
        xmlGenerator.createResourcesForLocalization(localization)
    }
}
