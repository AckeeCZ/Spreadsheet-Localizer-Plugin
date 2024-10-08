package cz.ackee.localizer.plugin.core.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param supportEmptyStrings true if empty strings can be declared in localization sheet, false if not. False means
 * that empty strings will be discarded for the given language in the localization sheet and plugin will not generate
 * the string resource for the given key at all. This value is optional for compatibility reasons and defaults to false,
 * if missing.
 */
@Serializable
data class LocalizationConfig(
    @SerialName("fileId") val fileId: String = "",
    @SerialName("sheetName") val sheetName: String = "",
    @SerialName("apiKey") val apiKey: String = "",
    @SerialName("serviceAccountPath") val serviceAccountPath: String = "",
    @SerialName("languageMapping") val languageMapping: Map<String, String?> = emptyMap(),
    @SerialName("resourcesFolderPath") val resourcesFolderPath: String = "",
    @SerialName("supportEmptyStrings") val supportEmptyStrings: Boolean = false,
)
