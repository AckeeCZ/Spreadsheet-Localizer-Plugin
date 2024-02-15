package cz.ackee.localizer.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.WindowManager
import cz.ackee.localizer.plugin.core.auth.CredentialsService
import cz.ackee.localizer.plugin.core.configuration.ConfigurationParser
import cz.ackee.localizer.plugin.core.localization.LocalizationsRepository
import cz.ackee.localizer.plugin.core.sheet.XmlGenerator
import cz.ackee.localizer.plugin.dialog.LocalizationDialog

/**
 * Action that shows params input dialog which downloads the localizations using the given data
 */
class LoadLocalizationAction : AnAction() {

    private val configurationParser = ConfigurationParser()
    private val credentialsService = CredentialsService()
    private val localizationsRepository = LocalizationsRepository()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)

        LocalizationDialog(project!!) { configPath ->
            try {
                val configuration = configurationParser.parse(configPath)
                val credentials = credentialsService.getCredentials(configuration)
                val localization = localizationsRepository.getLocalization(configuration, credentials)

                val xmlGenerator = XmlGenerator.from(configPath, configuration)
                xmlGenerator.createResourcesForLocalization(localization)

                ApplicationManager.getApplication().invokeLater {
                    VirtualFileManager.getInstance().syncRefresh()
                    WindowManager.getInstance().getStatusBar(project).info = "Resources generated successfully"
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, e.toString(), "Error")
                }
            }
        }.show()
    }
}
