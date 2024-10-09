package cz.ackee.localizer.plugin.dialog

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import cz.ackee.localizer.plugin.settings.LocalizerSettings
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Dialog that collects the Google Sheet information
 */
class LocalizationDialog(
    project: Project,
    private val listener: (String) -> Unit
) : DialogWrapper(project) {

    private lateinit var uiContainer: JPanel
    private lateinit var projectComboBox: JComboBox<String>
    private lateinit var editProjectName: JTextField
    private lateinit var editConfigPath: TextFieldWithBrowseButton
    private val settings = project.getService(LocalizerSettings::class.java)

    init {
        init()
        pack()
        val currentSettings = settings.currentProject
        bindProjectSettings(currentSettings)

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json").apply {
            title = "Choose Config File Path"
        }
        val listener = TextBrowseFolderListener(descriptor, project)
        editConfigPath.addBrowseFolderListener(listener)
        projectComboBox.model = DefaultComboBoxModel(settings.projects.map { it.projectName }.toTypedArray() + "New project")
        projectComboBox.selectedIndex = settings.selectedProject
        projectComboBox.addItemListener {
            bindProjectSettings(settings.projects.getOrNull(projectComboBox.selectedIndex))
        }
    }

    private fun bindProjectSettings(currentSettings: LocalizerSettings.ProjectSettings?) {
        editProjectName.text = currentSettings?.projectName ?: ""
        editConfigPath.text = currentSettings?.resPath ?: ""
    }

    override fun createCenterPanel(): JComponent {
        return uiContainer
    }

    override fun doOKAction() {
        val configPath = editConfigPath.text

        val file = File(configPath)
        if (!file.exists()) {
            Messages.showErrorDialog("You must choose the path to localizations configuration JSON file", "Invalid directory")
            return
        }

        settings.selectedProject = projectComboBox.selectedIndex

        val currentSettings = settings.currentProject ?: LocalizerSettings.ProjectSettings()
        currentSettings.projectName = if (editProjectName.text.isNullOrEmpty()) "Default" else editProjectName.text
        currentSettings.resPath = configPath
        if (projectComboBox.selectedIndex > settings.projects.lastIndex) {
            settings.projects += currentSettings
        }
        super.doOKAction()
        listener.invoke(configPath)
    }
}