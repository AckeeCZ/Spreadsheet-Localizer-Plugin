package cz.ackee.localizer.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.WindowManager
import cz.ackee.localizer.plugin.core.FetchConfiguration
import cz.ackee.localizer.plugin.core.LocalizationsFetcher
import cz.ackee.localizer.plugin.dialog.LocalizationDialog

/**
 * Action that shows params input dialog which downloads the localizations using the given data
 */
class LoadLocalizationAction : AnAction() {

    private val fetcher = LocalizationsFetcher()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)

        LocalizationDialog(project!!) { apiKey, sheetId, sheetName, defaultLang, resPath ->
            try {
                fetcher.fetch(
                    FetchConfiguration(
                        sheetId = sheetId,
                        listName = sheetName,
                        defaultLanguage = defaultLang,
                        resourcesFolderPath = resPath,
                        apiKey = apiKey
                    ))
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