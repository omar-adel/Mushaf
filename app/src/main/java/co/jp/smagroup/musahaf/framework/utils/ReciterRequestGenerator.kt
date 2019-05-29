package co.jp.smagroup.musahaf.framework.utils

import android.os.Environment
import co.jp.smagroup.musahaf.framework.commen.MediaSourceBuilder
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.model.Surah
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import java.io.File

/**
 * Created by ${User} on ${Date}
 */

object ReciterRequestGenerator {

    fun createRequestFromFile(reciterName: String, reciterId: String, surah: Surah, ayaNumber: Int): Request {
        val file = File(
            Environment.getExternalStorageDirectory(), recitersFolder(reciterName, surah) + "$ayaNumber.mp3"
        )
        return createRequest(ayaNumber, reciterId, file.absolutePath)
    }

    private fun createRequest(number: Int, selectedReciterId: String, filePath: String): Request =
        Request(MediaSourceBuilder.linkGenerator(number, selectedReciterId), filePath).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

    fun recitersFolder(reciterName: String, surah: Surah) =
        "${MusahafConstants.AppName}/reciters/$reciterName/${surah.number}/"

}
