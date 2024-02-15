package cz.ackee.localizer.plugin.core.localization

/**
 * Response from Google Sheets API
 */
data class GoogleSheetResponse(
    val range: String,
    val majorDimension: String,
    val values: List<List<String>>
)
