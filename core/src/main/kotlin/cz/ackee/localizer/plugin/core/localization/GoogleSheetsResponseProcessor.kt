package cz.ackee.localizer.plugin.core.localization

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import cz.ackee.localizer.plugin.core.sheet.GoogleSheetResponse
import cz.ackee.localizer.plugin.core.sheet.XmlGenerator
import java.io.File

class GoogleSheetsResponseProcessor {

    fun processGoogleSheetsResponse(configPath: String, googleSheetResponse: GoogleSheetResponse, configuration: LocalizationConfig) {
        val xmlGenerator = XmlGenerator(File(File(configPath).parent, configuration.resourcesFolderPath))
        val localization = Localization.fromGoogleResponse(googleSheetResponse, configuration)
        xmlGenerator.createResourcesForLocalization(localization)
    }
}
