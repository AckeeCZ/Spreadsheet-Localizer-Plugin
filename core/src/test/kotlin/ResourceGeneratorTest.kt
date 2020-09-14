import cz.ackee.localizer.plugin.core.Localization
import cz.ackee.localizer.plugin.core.XmlGenerator
import org.intellij.lang.annotations.Language
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Test for resource generator. Tests if the generated input is valid from provided [Localization].
 */
class ResourceGeneratorTest {

    val resourceGenerator = XmlGenerator("test")

    val enResource = Localization.Resource("en", listOf(
            Localization.Resource.Entry.Section("Section1"),
            Localization.Resource.Entry.Key("key1.android", "Value1"),
            Localization.Resource.Entry.Section("Section2"),
            Localization.Resource.Entry.Key("key2.android", "Value2"),
            Localization.Resource.Entry.Key("key3.android", "Value3")
    ))

    val csResource = Localization.Resource("cs", listOf(
            Localization.Resource.Entry.Section("Sekce1"),
            Localization.Resource.Entry.Key("klic1.android", "Hodnota1"),
            Localization.Resource.Entry.Section("Sekce2"),
            Localization.Resource.Entry.Key("klic2.android", "Hodnota2"),
            Localization.Resource.Entry.Key("klic3.android", "Hodnota3")
    ))

    val enXml = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "<!-- Section1 -->\n" +
            "<string name=\"key1.android\">Value1</string>\n" +
            "<!-- Section2 -->\n" +
            "<string name=\"key2.android\">Value2</string>\n" +
            "<string name=\"key3.android\">Value3</string>\n" +
            "</resources>").replace("\n", "")

    val csXml = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "<!-- Sekce1 -->\n" +
            "<string name=\"klic1.android\">Hodnota1</string>\n" +
            "<!-- Sekce2 -->\n" +
            "<string name=\"klic2.android\">Hodnota2</string>\n" +
            "<string name=\"klic3.android\">Hodnota3</string>\n" +
            "</resources>").replace("\n", "")

    @After
    fun clean() {
        File("test").delete()
    }

    @Test
    fun shouldGenerateResourceForOneLanguage() {
        val localization = Localization(listOf(enResource))
        resourceGenerator.createResourcesForLocalization(localization)
        val file = File("test/values/strings.xml")
        assert(file.exists())
        assertEquals(
                enXml,
                file.readText().replace("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$".toRegex(), "").replace("\n", "").replace("\r", "").trim()
        )
    }

    @Test
    fun shouldGenerateResourcesForMultipleLanguages() {
        val localization = Localization(listOf(enResource, csResource))
        resourceGenerator.createResourcesForLocalization(localization)
        val fileEn = File("test/values/strings.xml")
        val fileCs = File("test/values-cs/strings.xml")
        assert(fileEn.exists())
        assert(fileCs.exists())
        assertEquals(
                enXml,
                fileEn.readText().replace("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$".toRegex(), "").replace("\n", "").replace("\r", "").trim()
        )
        assertEquals(
                csXml,
                fileCs.readText().replace("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$".toRegex(), "").replace("\n", "").replace("\r", "").trim()
        )
    }

    @Test
    fun shouldGeneratePluralsProperly() {
        val localization = Localization(
            listOf(
                Localization.Resource("en", listOf(
                    Localization.Resource.Entry.Plural("pluralKey", mutableMapOf(
                        Pair("few", "values"),
                        Pair("other", "value")
                    ))
                ))
            )
        )
        resourceGenerator.createResourcesForLocalization(localization)
        assertEquals(
                ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<resources>\n" +
                        "<plurals name=\"pluralKey\">" +
                        "<item quantity=\"few\">values</item>" +
                        "<item quantity=\"other\">value</item>" +
                        "</plurals>\n" +
                        "</resources>").replace("\n", ""),
                File("test/values/strings.xml").readText().replace("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$".toRegex(), "").replace("\n", "").replace("\r", "").trim()
        )
    }

    @Test
    fun shouldGenerateProperHtml() {
        val html = """<![CDATA[Jsem politicky exponovaná osoba<br /><a href=\"https://zonky.cz\">Co to znamená?</a>]]>"""
        val localization = Localization(
            listOf(
                Localization.Resource("en", listOf(
                    Localization.Resource.Entry.Key("keyHtml", html)
                ))
            )
        )
        resourceGenerator.createResourcesForLocalization(localization)
        assertEquals(
                html,
                File("test/values/strings.xml").readText().substringAfter("<string name=\"keyHtml\">").substringBefore("</string>")
        )
    }

    @Test
    fun shouldHandleSymbolsProperly() {
        val string = """If you don't want to mess \ahoj with George & his gang, pay your &gt;25% each month through B&B"""
        val localization = Localization(
            listOf(
                Localization.Resource("en", listOf(
                    Localization.Resource.Entry.Key("key", string)
                ))
            )
        )
        resourceGenerator.createResourcesForLocalization(localization)
        assertEquals(
                "If you don\\'t want to mess \\ahoj with George &amp; his gang, pay your &gt;25% each month through B&amp;B",
                File("test/values/strings.xml").readText().substringAfter("<string name=\"key\">").substringBefore("</string>")
        )
    }

    /**
     * This case caused a lot of bugs - last section did not have any keys in it
     */
    @Test
    fun `should generate empty last section`() {
        val localization = Localization(listOf(Localization.Resource(
            suffix = "en",
            entries = listOf(
                Localization.Resource.Entry.Section("Section A"),
                Localization.Resource.Entry.Key("key_1", "Value 1"),
                Localization.Resource.Entry.Section("Section B")
            )
        )))
        resourceGenerator.createResourcesForLocalization(localization)
        @Language("xml")
        val xml = """
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- Section A -->
    <string name="key_1">Value 1</string>

    <!-- Section B -->
</resources>
        """.trimIndent()
        assertEquals(
                xml,
                File("test/values/strings.xml").readText()
        )
    }
}