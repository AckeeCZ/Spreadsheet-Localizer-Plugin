package cz.ackee.localizer.plugin.core.sheet

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig
import cz.ackee.localizer.plugin.core.localization.Localization
import java.io.File

/**
 * Generates the resources XML files
 */
class XmlGenerator(
    private val resFolder: File,
    private val supportEmptyStrings: Boolean,
) {

    companion object {

        const val INDENT = "    "

        fun from(configurationPath: String, configuration: LocalizationConfig): XmlGenerator {
            val resFolder = File(File(configurationPath).parent, configuration.resourcesFolderPath)
            return XmlGenerator(
                resFolder = resFolder,
                supportEmptyStrings = configuration.supportEmptyStrings,
            )
        }
    }

    fun createResourcesForLocalization(localization: Localization) {
        localization.resources.forEach {
            createXmlForResource(it)
        }
    }

    private fun createXmlForResource(resource: Localization.Resource) {
        val valuesDirName = resource.suffix?.let { "values-${resource.suffix}" } ?: "values"
        val dir = File(resFolder, valuesDirName)
        dir.mkdirs()

        val builder = StringBuilder().apply {
            appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            append("<resources>")

            resource.entries.forEachIndexed { _, entry ->
                when (entry) {
                    is Localization.Resource.Entry.Section -> {
                        appendLine()
                        appendLine()
                        append(INDENT)
                        append("<!-- ${entry.name} -->")
                    }
                    is Localization.Resource.Entry.Key -> {
                        val formattedValue = entry.value.format()
                        if (formattedValue.isEmpty() && supportEmptyStrings) {
                            appendLine()
                            append(INDENT)
                            append("""<string name="${entry.key}"/>""")
                        } else if (formattedValue.isNotEmpty()) {
                            appendLine()
                            append(INDENT)
                            append("""<string name="${entry.key}">""")
                            append(entry.value.format())
                            append("</string>")
                        }
                    }
                    is Localization.Resource.Entry.Plural -> {
                        if (entry.values.any { it.value.isNotEmpty() }) {
                            appendLine()
                            append(INDENT)
                            append("<plurals name=\"${entry.key}\">")
                            entry.values.forEach { (key, value) ->
                                if (value.isNotEmpty()) {
                                    appendLine()
                                    append(INDENT)
                                    append(INDENT)
                                    append("<item quantity=\"$key\">")
                                    append(value.format())
                                    append("</item>")
                                }
                            }
                            appendLine()
                            append(INDENT)
                            append("</plurals>")
                        }
                    }
                }
            }
            appendLine().append("</resources>")
            appendLine()
        }

        val file = File(dir, "strings.xml")
        file.writeText(builder.toString())
    }

    private fun String.format() =
        this
            .replace("'", "\\'")
            .apply {
                if (!contains("CDATA")) {
                    replace("\"", "\\\"")
                }
            }
            .replace("&(?!.{2,4};)".toRegex(), "&amp;")
}
