package cz.ackee.localizer.settings

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Definition of the plugin's configuration
 */
class LocalizerConfigurable(val project: Project) : SearchableConfigurable {

    private var settings: LocalizerSettings? = null
    private val sheetIdField = JBTextField()
    private val sheetNameField = JBTextField()
    private val defaultLangField = JBTextField()
    private val pathField = TextFieldWithBrowseButton()

    override fun getId(): String {
        return "settings.localizer.add"
    }

    override fun isModified(): Boolean {
        return sheetIdField.text != settings?.sheetId && sheetNameField.text != settings?.sheetName &&
                pathField.text != settings?.resPath && defaultLangField.text != settings?.defaultLang
    }

    override fun getDisplayName(): String {
        return "Localizer"
    }

    override fun apply() {
        settings?.sheetId = sheetIdField.text
        settings?.sheetName = sheetNameField.text
        settings?.defaultLang = defaultLangField.text
        settings?.resPath = pathField.text
    }

    override fun reset() {
        val currentSettings = settings
        if (currentSettings != null) {
            sheetIdField.text = currentSettings.sheetId
            sheetNameField.text = currentSettings.sheetName
            defaultLangField.text = currentSettings.defaultLang
            pathField.text = currentSettings.resPath
        }
    }

    override fun createComponent(): JComponent {
        settings = ServiceManager.getService(project, LocalizerSettings::class.java)
        val builder = FormBuilder.createFormBuilder()
        builder.addLabeledComponent("Google sheet ID", sheetIdField)
        builder.addLabeledComponent("Sheet name", sheetNameField)
        builder.addLabeledComponent("Default language", defaultLangField)
        builder.addLabeledComponent("Resources path", pathField)

        val panel = builder.panel
        val wrapper = JPanel(BorderLayout())
        wrapper.add(panel, BorderLayout.NORTH)
        return wrapper
    }
}