package cz.ackee.localizer.dialog

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import cz.ackee.localizer.settings.LocalizerSettings
import java.io.File
import javax.swing.*

/**
 * Dialog that collects the Google Sheet information
 */
class LocalizationDialog(project: Project,
                         val listener: (String, String, String, String, String) -> Unit) : DialogWrapper(project) {

    private lateinit var uiContainer: JPanel
    private lateinit var projectComboBox: JComboBox<String>
    private lateinit var editApiKey: JTextField
    private lateinit var editProjectName: JTextField
    private lateinit var editId: JTextField
    private lateinit var editSheetName: JTextField
    private lateinit var editDefaultLang: JTextField
    private lateinit var editPath: TextFieldWithBrowseButton
    private val settings = ServiceManager.getService(project, LocalizerSettings::class.java)

    init {
        init()
        pack()
        val currentSettings = settings.currentProject
        bindProjectSettings(currentSettings)

        editPath.addBrowseFolderListener(
                "Choose path",
                "description",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
        projectComboBox.model = DefaultComboBoxModel(settings.projects.map { it.projectName }.toTypedArray() + "New project")
        projectComboBox.selectedIndex = settings.selectedProject
        projectComboBox.addItemListener {
            bindProjectSettings(settings.projects.getOrNull(projectComboBox.selectedIndex))
        }
    }

    private fun bindProjectSettings(currentSettings: LocalizerSettings.ProjectSettings?) {
        editProjectName.text = currentSettings?.projectName ?: ""
        editApiKey.text = currentSettings?.apiKey ?: ""
        editId.text = currentSettings?.sheetId ?: ""
        editSheetName.text = currentSettings?.sheetName ?: ""
        editDefaultLang.text = currentSettings?.defaultLang ?: ""
        editPath.text = currentSettings?.resPath ?: ""
    }

    override fun createCenterPanel(): JComponent {
        return uiContainer
    }

    override fun doOKAction() {
        val apiKey = editApiKey.text
        val id = editId.text
        val sheetName = editSheetName.text
        val path = editPath.text
        val defaultLang = editDefaultLang.text
        if (apiKey.isEmpty()) {
            Messages.showErrorDialog("Please enter your Google API key", "Google API key is empty")
            return
        }
        if (id.isEmpty()) {
            Messages.showErrorDialog("Please enter Google sheet ID", "ID is empty")
            return
        }
        if (sheetName.isEmpty()) {
            Messages.showErrorDialog("Please enter Sheet name", "Name is empty")
            return
        }
        val resDir = File(path)
        if (!resDir.exists() || resDir.nameWithoutExtension != "res") {
            Messages.showErrorDialog("You should choose the path to resources directory of your Android project", "Invalid directory")
            return
        }

        settings.selectedProject = projectComboBox.selectedIndex

        val currentSettings = settings.currentProject ?: LocalizerSettings.ProjectSettings()
        currentSettings.projectName = if (editProjectName.text.isNullOrEmpty()) "Default" else editProjectName.text
        currentSettings.apiKey = apiKey
        currentSettings.sheetId = id
        currentSettings.sheetName = sheetName
        currentSettings.defaultLang = defaultLang
        currentSettings.resPath = path
        if (projectComboBox.selectedIndex > settings.projects.lastIndex) {
            settings.projects += currentSettings
        }
        super.doOKAction()
        listener.invoke(apiKey, id, sheetName, defaultLang, path)
    }
}