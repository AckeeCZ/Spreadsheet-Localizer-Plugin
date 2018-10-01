import cz.ackee.localizer.model.GoogleSheetResponse
import cz.ackee.localizer.model.Localization
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * Tests for [Localization] object generation from Google sheet response.
 */
class LocalizationTest {

    @Test
    fun shouldCreateLocalizationFromResponse() {
        val response = GoogleSheetResponse(
                range = "",
                majorDimension = "",
                values = listOf(
                        listOf("section", "key_android", "key_whatever", "EN", "CS"),
                        listOf("Section1"),
                        listOf("", "", "key1.whatever", "Value1", "Value1CS"),
                        listOf("", "key2.android", "key2.whatever", "Value2", "Value2CS"),
                        listOf("Section2"),
                        listOf("", "key3.android", "", "Value3", "Value3CS"),
                        listOf("", "key4.android", "key4.whatever", "Value4", "Value4CS")
                )
        )
        val localization = Localization.fromGoogleResponse(response, Localization.DEFAULT_LANGUAGES)
        assertEquals(
                Localization(
                        listOf(
                                Localization.Resource("en", listOf(
                                        Localization.Resource.Entry.Section("Section1"),
                                        Localization.Resource.Entry.Key("key2.android", "Value2"),
                                        Localization.Resource.Entry.Section("Section2"),
                                        Localization.Resource.Entry.Key("key3.android", "Value3"),
                                        Localization.Resource.Entry.Key("key4.android", "Value4")
                                )),
                                Localization.Resource("cs", listOf(
                                        Localization.Resource.Entry.Section("Section1"),
                                        Localization.Resource.Entry.Key("key2.android", "Value2CS"),
                                        Localization.Resource.Entry.Section("Section2"),
                                        Localization.Resource.Entry.Key("key3.android", "Value3CS"),
                                        Localization.Resource.Entry.Key("key4.android", "Value4CS")
                                ))
                        )
                ),
                localization
        )
    }
}