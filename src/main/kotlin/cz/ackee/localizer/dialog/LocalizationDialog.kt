package cz.ackee.localizer.dialog

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import cz.ackee.localizer.settings.LocalizerSettings
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Dialog that collects the Google Sheet information
 */
class LocalizationDialog(project: Project,
                         val listener: (String, String, String, String, String) -> Unit) : DialogWrapper(project) {

    private lateinit var uiContainer: JPanel
    private lateinit var editApiKey: JTextField
    private lateinit var editId: JTextField
    private lateinit var editSheetName: JTextField
    private lateinit var editDefaultLang: JTextField
    private lateinit var editPath: TextFieldWithBrowseButton
    private val settings = ServiceManager.getService(project, LocalizerSettings::class.java)

    init {
        init()
        pack()

        editApiKey.text = settings.apiKey
        editId.text = settings.sheetId
        editSheetName.text = settings.sheetName
        editDefaultLang.text = settings.defaultLang
        editPath.text = settings.resPath

        editPath.addBrowseFolderListener(
                "Choose path",
                "description",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
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

        settings.apiKey = apiKey
        settings.sheetId = id
        settings.sheetName = sheetName
        settings.defaultLang = defaultLang
        settings.resPath = path
        super.doOKAction()
        listener.invoke(apiKey, id, sheetName, defaultLang, path)
    }
}