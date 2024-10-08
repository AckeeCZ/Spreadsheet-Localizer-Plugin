package cz.ackee.localizer.plugin.core.sheet

import cz.ackee.localizer.plugin.core.localization.Localization
import org.amshove.kluent.shouldBeEqualTo
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Test for [XmlGenerator]. Tests if the generated input is valid from provided [Localization].
 */
class XmlGeneratorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val resFolder by lazy { temporaryFolder.newFolder("res") }

    private val enResource = Localization.Resource(
        null, listOf(
            Localization.Resource.Entry.Section("Section1"),
            Localization.Resource.Entry.Key("key1.android", "Value1"),
            Localization.Resource.Entry.Section("Section2"),
            Localization.Resource.Entry.Key("key2.android", "Value2"),
            Localization.Resource.Entry.Key("key3.android", "Value3")
        )
    )

    private val csResource = Localization.Resource(
        "cs", listOf(
            Localization.Resource.Entry.Section("Sekce1"),
            Localization.Resource.Entry.Key("klic1.android", "Hodnota1"),
            Localization.Resource.Entry.Section("Sekce2"),
            Localization.Resource.Entry.Key("klic2.android", "Hodnota2"),
            Localization.Resource.Entry.Key("klic3.android", "Hodnota3")
        )
    )

    private val enXml = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<resources>\n" +
        "<!-- Section1 -->\n" +
        "<string name=\"key1.android\">Value1</string>\n" +
        "<!-- Section2 -->\n" +
        "<string name=\"key2.android\">Value2</string>\n" +
        "<string name=\"key3.android\">Value3</string>\n" +
        "</resources>").replace("\n", "")

    private val csXml = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<resources>\n" +
        "<!-- Sekce1 -->\n" +
        "<string name=\"klic1.android\">Hodnota1</string>\n" +
        "<!-- Sekce2 -->\n" +
        "<string name=\"klic2.android\">Hodnota2</string>\n" +
        "<string name=\"klic3.android\">Hodnota3</string>\n" +
        "</resources>").replace("\n", "")

    private val fileEn by lazy { File(resFolder, "values/strings.xml") }
    private val fileCs by lazy { File(resFolder, "values-cs/strings.xml") }
    private val defaultStringsFile by lazy { File(resFolder, "values/strings.xml") }

    fun createSut(supportEmptyStrings: Boolean = false): XmlGenerator {
        return XmlGenerator(resFolder, supportEmptyStrings = supportEmptyStrings)
    }

    @Test
    fun shouldGenerateResourceForOneLanguage() {
        val localization = Localization(listOf(enResource))
        createSut().createResourcesForLocalization(localization)
        assert(defaultStringsFile.exists())
        assertEquals(enXml, defaultStringsFile.readAndNormalizeXml())
    }

    private fun File.readAndNormalizeXml(): String {
        return readText()
            .replace("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$".toRegex(), "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()
    }

    @Test
    fun shouldGenerateResourcesForMultipleLanguages() {
        val localization = Localization(listOf(enResource, csResource))
        createSut().createResourcesForLocalization(localization)
        assert(fileEn.exists())
        assert(fileCs.exists())
        assertEquals(enXml, fileEn.readAndNormalizeXml())
        assertEquals(csXml, fileCs.readAndNormalizeXml())
    }

    @Test
    fun shouldGeneratePluralsProperly() {
        val localization = Localization(
            listOf(
                Localization.Resource(
                    null, listOf(
                        Localization.Resource.Entry.Plural(
                            "pluralKey", mutableMapOf(
                                Pair("few", "values"),
                                Pair("other", "value")
                            )
                        )
                    )
                )
            )
        )
        createSut().createResourcesForLocalization(localization)
        assertEquals(
            ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "<plurals name=\"pluralKey\">" +
                "<item quantity=\"few\">values</item>" +
                "<item quantity=\"other\">value</item>" +
                "</plurals>\n" +
                "</resources>").replace("\n", ""),
            defaultStringsFile.readAndNormalizeXml()
        )
    }

    @Test
    fun shouldGenerateProperHtml() {
        val html = """<![CDATA[Jsem politicky exponovaná osoba<br /><a href=\"https://zonky.cz\">Co to znamená?</a>]]>"""
        val localization = Localization(
            listOf(
                Localization.Resource(
                    null, listOf(
                        Localization.Resource.Entry.Key("keyHtml", html)
                    )
                )
            )
        )
        createSut().createResourcesForLocalization(localization)
        assertEquals(
            html,
            defaultStringsFile.readText().substringAfter("<string name=\"keyHtml\">").substringBefore("</string>")
        )
    }

    @Test
    fun shouldHandleSymbolsProperly() {
        val string = """If you don't want to mess \ahoj with George & his gang, pay your &gt;25% each month through B&B"""
        val localization = Localization(
            listOf(
                Localization.Resource(
                    null, listOf(
                        Localization.Resource.Entry.Key("key", string)
                    )
                )
            )
        )
        createSut().createResourcesForLocalization(localization)
        assertEquals(
            "If you don\\'t want to mess \\ahoj with George &amp; his gang, pay your &gt;25% each month through B&amp;B",
            defaultStringsFile.readText().substringAfter("<string name=\"key\">").substringBefore("</string>")
        )
    }

    /**
     * This case caused a lot of bugs - last section did not have any keys in it
     */
    @Test
    fun `should generate empty last section`() {
        val localization = Localization(
            listOf(
                Localization.Resource(
                    suffix = null,
                    entries = listOf(
                        Localization.Resource.Entry.Section("Section A"),
                        Localization.Resource.Entry.Key("key_1", "Value 1"),
                        Localization.Resource.Entry.Section("Section B")
                    )
                )
            )
        )
        createSut().createResourcesForLocalization(localization)
        @Language("xml")
        val xml = """
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- Section A -->
    <string name="key_1">Value 1</string>

    <!-- Section B -->
</resources>
        """.trimIndent()
        assertEquals(xml, defaultStringsFile.readText().trimIndent())
    }

    @Test
    fun `should include empty entry value if configured to support it`() {
        testEmptyStringInclusion(
            supportEmptyStrings = true,
            getExpectedStringElement = { key -> """<string name="$key"/>""" },
        )
    }

    private fun testEmptyStringInclusion(
        supportEmptyStrings: Boolean,
        getExpectedStringElement: (String) -> String,
    ) {
        val key = "key"
        val resource = Localization.Resource(
            suffix = null,
            entries = listOf(Localization.Resource.Entry.Key(key, "")),
        )
        val localization = Localization(listOf(resource))

        createSut(supportEmptyStrings = supportEmptyStrings).createResourcesForLocalization(localization)

        val actual = defaultStringsFile.readAndNormalizeXml()
        val expected = """<?xml version="1.0" encoding="utf-8"?><resources>${getExpectedStringElement(key)}</resources>"""
        actual shouldBeEqualTo expected
    }

    @Test
    fun `should not include empty entry value if configured to not support it`() {
        testEmptyStringInclusion(
            supportEmptyStrings = false,
            getExpectedStringElement = { "" },
        )
    }
}
