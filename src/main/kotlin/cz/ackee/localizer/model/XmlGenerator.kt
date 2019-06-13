package cz.ackee.localizer.model

import java.io.File

/**
 * Generates the resources XML files
 */
class XmlGenerator(val resPath: String, val defaultLang: String = "en") {

    companion object {
        const val INDENT = "    "
    }

    fun createResourcesForLocalization(localization: Localization) {
        localization.resources.forEach {
            createXmlForResource(it)
        }
    }

    private fun createXmlForResource(resource: Localization.Resource) {

        val valuesDirName = if (defaultLang != resource.suffix) "values-${resource.suffix}" else "values"
        val dir = File("$resPath/$valuesDirName")
        dir.mkdirs()

        val builder = StringBuilder().apply {
            appendln("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            append("<resources>")

            resource.entries.forEachIndexed { index, entry ->
                when (entry) {
                    is Localization.Resource.Entry.Section -> {
                        if (resource.entries[index + 1] is Localization.Resource.Entry.Section) {
                            return@forEachIndexed
                        }
                        appendln()
                        appendln()
                        append(INDENT)
                        append("<!-- ${entry.name} -->")
                    }
                    is Localization.Resource.Entry.Key -> {
                        // do not insert empty texts
                        if (entry.value.format().isNotEmpty()) {
                            appendln()
                            append(INDENT)
                            append("<string name=\"${entry.key}\">")
                            append(entry.value.format())
                            append("</string>")
                        }
                    }
                    is Localization.Resource.Entry.Plural -> {
                        if (entry.values.any { it.value.isNotEmpty() }) {
                            appendln()
                            append(INDENT)
                            append("<plurals name=\"${entry.key}\">")
                            entry.values.forEach { key, value ->
                                if (value.isNotEmpty()) {
                                    appendln()
                                    append(INDENT)
                                    append(INDENT)
                                    append("<item quantity=\"$key\">")
                                    append(value.format())
                                    append("</item>")
                                }
                            }
                            appendln()
                            append(INDENT)
                            append("</plurals>")
                        }
                    }
                }
            }
            appendln().append("</resources>")
        }

        val file = File(dir, "strings.xml")
        file.writeText(builder.toString())
    }

    private fun String.format() =
            this
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("&(?!.{2,4};)".toRegex(), "&amp;")
}