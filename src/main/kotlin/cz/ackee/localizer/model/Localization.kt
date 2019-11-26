package cz.ackee.localizer.model

/**
 * Localization object represents data from sheet response in resource files-oriented way. Each resource object
 * contains list of different rows for one language.
 */
data class Localization(val resources: List<Resource>) {

    companion object {
        val DEFAULT_LANGUAGES =
            listOf("aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs",
                "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee", "el", "en", "eo", "es", "et", "eu", "fa", "ff",
                "fi", "fj", "fo", "fr", "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id",
                "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku",
                "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my",
                "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu",
                "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv",
                "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo",
                "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu")

        fun fromGoogleResponse(response: GoogleSheetResponse,
                               languages: List<String> = DEFAULT_LANGUAGES): Localization {

            // index of section column, null if section column doesn't exist
            var sectionIndex: Int? = null

            // looking for valid column IDs (section column, android key column and supported languages keys)
            val validColumnIds = response.values[0].mapIndexed { index, name ->
                if (name.contains("section", ignoreCase = true) ||
                        name.contains("android", ignoreCase = true) ||
                        languages.contains(name.toLowerCase())) {
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
                            .mapIndexed { index, suffix ->
                                // now we have index for particular language and its suffix

                                val entries = mutableListOf<Resource.Entry>()

                                // starting to accumulate quantities for plural, reset to null if the next row is another plural or key
                                var pluralAccumulator: Resource.Entry.Plural? = null

                                // iterate through each row and take values for particular language (outer cyclus)
                                filteredValues.forEach { row ->
                                    if (row is XmlRow.Key) {
                                        // if this is a key row
                                        val key = row.cells[0] // take its key
                                        val value = row.cells.getOrNull(index + 1) ?: "" // take its value for this language (+1 due to key)
                                        if (key.contains("##")) {
                                            // if this is a plural string (contains "##")
                                            val pluralKey = key.substringBefore("##") // take its key (all before "##")
                                            if (pluralAccumulator == null) {
                                                // if it is first plural string in sequence
                                                pluralAccumulator = Resource.Entry.Plural(pluralKey, mutableMapOf()) // start to accumulate values for this key
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
                                Resource(normalizeSuffix(suffix), entries)
                            }
            )
        }

        private fun normalizeSuffix(suffix: String): String {
            val suff = suffix.toLowerCase()
            return if (suff == "cz") "cs" else suff
        }
    }

    data class Resource(val suffix: String,
                        val entries: List<Resource.Entry>) {

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
