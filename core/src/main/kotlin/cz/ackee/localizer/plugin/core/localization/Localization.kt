package cz.ackee.localizer.plugin.core.localization

import cz.ackee.localizer.plugin.core.configuration.LocalizationConfig

/**
 * Localization object represents data from sheet response in resource files-oriented way. Each resource object
 * contains list of different rows for one language.
 */
data class Localization(val resources: List<Resource>) {

    companion object {

        fun fromGoogleResponse(response: GoogleSheetResponse, configuration: LocalizationConfig): Localization {

            // index of section column, null if section column doesn't exist
            var sectionIndex: Int? = null

            // looking for valid column IDs (section column, android key column and supported languages keys)
            val validColumnIds = response.values[0].mapIndexed { index, name ->
                if (name.contains("section", ignoreCase = true) ||
                    name.contains("android", ignoreCase = true) ||
                    configuration.languageMapping.keys.contains(name)) {
                    if (name.contains("section", ignoreCase = true)) {
                        sectionIndex = index
                    }
                    index
                } else {
                    null
                }
            }.filterNotNull() // if column isn't valid, return null and filter it

            /*
            Next step is transforming Google sheet JSON to list of [XmlRow]s (more logical representation for one row,
            with omitted redundant values). Rows may be the header row (first row with keys and titles), section row
            (if the section column exists, it contains only section name) or key row (represents a key with a list of
            values for all languages).
             */
            val filteredValues = response.values
                .map { it.filterIndexed { index, _ -> index in validColumnIds } } // filter invalid columns
                .filter { it.isNotEmpty() } // filter empty rows
                .mapIndexed { index, list ->
                    if (index == 0) {
                        // first row is always a header row
                        XmlRow.Header(list)
                    } else {
                        if (sectionIndex != null) {
                            // if section column exists
                            if (list[sectionIndex!!].isNotEmpty()) {
                                // and its value for this row isn't empty
                                XmlRow.Section(list[sectionIndex!!]) // this is the section row
                            } else {
                                XmlRow.Key(list.drop(1)) // this is key row, dropping the first value (from section column)
                            }
                        } else {
                            XmlRow.Key(list) // if section column doesn't exist, all other rows are key rows
                        }
                    }
                }
                .filterNot { it is XmlRow.Key && it.cells[0].isEmpty() } // filtering all key rows without a key

            /*
            The last step is converting the list of rows into resource representations for each language. Each language
            is represented by [Resource] which contains language (suffix for values directory name) and the list of
            XML entries. Entries may be section entry (will be converted to XML comment just for visual organization),
            key entry (represents one string key with value) and plural entry (represents one plural resource with the
            list of quantities).
             */
            return Localization(
                // iterating through all languages (columns with values)
                (filteredValues[0] as XmlRow.Header).cells
                    .drop(if (sectionIndex == null) 1 else 2) // drop key column and section column if exists
                    .map { configuration.languageMapping[it] } // get the suffix from mapping based on column key
                    .mapIndexed { index, suffix ->
                        // now we have index for particular language and its suffix
                        val entries = mutableListOf<Resource.Entry>()

                        // starting to accumulate quantities for plural, reset to null if the next row is another plural or key
                        var pluralAccumulator: Resource.Entry.Plural? = null

                        // iterate through each row and take values for particular language (outer cycle)
                        filteredValues.forEach { row ->
                            if (row is XmlRow.Key) {
                                // if this is a key row
                                val key = row.cells[0] // take its key
                                val value = row.cells.getOrNull(index + 1)
                                    ?: "" // take its value for this language (+1 due to key)
                                if (key.contains("##")) {
                                    // if this is a plural string (contains "##")
                                    val pluralKey = key.substringBefore("##") // take its key (all before "##")
                                    if (pluralAccumulator == null) {
                                        // if it is first plural string in sequence
                                        pluralAccumulator =
                                            Resource.Entry.Plural(pluralKey, mutableMapOf()) // start to accumulate values for this key
                                    } else if (pluralKey != pluralAccumulator!!.key) {
                                        // if the accumulator already contains something, but the key differs, add plural entry to resource and start accumulate for new key
                                        entries.add(pluralAccumulator!!)
                                        pluralAccumulator = Resource.Entry.Plural(pluralKey, mutableMapOf())
                                    }
                                    // add quantity key and value to plural accumulator
                                    pluralAccumulator!!.values.put(key.substringAfter("##{").replace("}", ""), value)
                                } else {
                                    // if this is an ordinary key row (not plural)
                                    if (pluralAccumulator != null) {
                                        // if there is something in plural accumulator, add it to resource and reset accumulator
                                        entries.add(pluralAccumulator!!)
                                        pluralAccumulator = null
                                    }
                                    entries.add(Resource.Entry.Key(key, value)) // add key-value to resource
                                }
                            } else if (row is XmlRow.Section) {
                                // if the row is section
                                if (pluralAccumulator != null) {
                                    // if there is something in plural accumulator, add it to resource and reset accumulator
                                    entries.add(pluralAccumulator!!)
                                    pluralAccumulator = null
                                }
                                entries.add(Resource.Entry.Section(row.name)) // add section to resource
                            }
                        }
                        // on the end of iteration, if there is something in plural accumulator, add it to resource, reset
                        if (pluralAccumulator != null) {
                            entries.add(pluralAccumulator!!)
                            pluralAccumulator = null
                        }
                        Resource(suffix, entries)
                    }
            )
        }
    }

    data class Resource(
        val suffix: String?,
        val entries: List<Entry>
    ) {

        sealed class Entry {
            data class Section(val name: String) : Entry()
            data class Key(val key: String, val value: String) : Entry()
            data class Plural(val key: String, val values: MutableMap<String, String>) : Entry()
        }
    }
}

sealed class XmlRow {
    data class Header(val cells: List<String>) : XmlRow()
    data class Section(val name: String) : XmlRow()
    data class Key(val cells: List<String>) : XmlRow()
}
