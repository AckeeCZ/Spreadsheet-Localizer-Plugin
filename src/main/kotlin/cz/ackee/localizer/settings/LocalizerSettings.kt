package cz.ackee.localizer.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent component to save settings
 */
@State(name = "LocalizerSettings", storages = arrayOf(Storage("localizerSettings.xml")))
class LocalizerSettings : PersistentStateComponent<LocalizerSettings> {

    var apiKey: String = ""
    var sheetId: String = ""
    var sheetName: String = ""
    var defaultLang: String = "en"
    var resPath: String = ""

    override fun getState(): LocalizerSettings {
        return this
    }

    override fun loadState(state: LocalizerSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}