package cz.ackee.localizer.plugin.core.util

import java.io.File

object FileUtils {

    fun createTmpFile(path: String) {
        val file = File(path)
        file.createNewFile()
        file.deleteOnExit()
    }
}
