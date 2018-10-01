package cz.ackee.localizer.action

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.WindowManager
import com.squareup.moshi.Moshi
import cz.ackee.localizer.dialog.LocalizationDialog
import cz.ackee.localizer.model.GoogleSheetResponse
import cz.ackee.localizer.model.Localization
import cz.ackee.localizer.model.XmlGenerator

/**
 * Action that shows params input dialog which downloads the localizations using the given data
 */
class LoadLocalizationAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)

        LocalizationDialog(project!!) { apiKey, sheetId, sheetName, defaultLang, resPath ->
            "https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values/$sheetName?key=$apiKey".httpGet()
                    .header(Pair("content-type", "application/json; charset=UTF-8"))
                    .responseString { _, _, result ->
                        if (result is Result.Success) {
                            val response = Moshi.Builder().build().adapter(GoogleSheetResponse::class.java).fromJson(result.component1()!!)!!

                            // create our localization object from parsed response
                            val xmlGenerator = XmlGenerator(resPath, defaultLang)
                            val localization = Localization.fromGoogleResponse(response)
                            xmlGenerator.createResourcesForLocalization(localization)

                            ApplicationManager.getApplication().invokeLater {
                                VirtualFileManager.getInstance().syncRefresh()
                                WindowManager.getInstance().getStatusBar(project).info = "Resources generated successfully"
                            }
                        } else if (result is Result.Failure) {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showErrorDialog(project, result.toString(), "Error")
                            }
                        }
                    }
        }.show()
    }
}