package cz.ackee.localizer.plugin.core.localization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Google Sheets API
 */
@Serializable
data class GoogleSheetResponse(
    @SerialName("range") val range: String,
    @SerialName("majorDimension") val majorDimension: String,
    @SerialName("values") val values: List<List<String>>
)
