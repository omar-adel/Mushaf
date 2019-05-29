package co.jp.smagroup.musahaf.framework.api

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.commen.Priority
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Reciter
import co.jp.smagroup.musahaf.ui.quran.read.reciter.DownloadingFragment
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.widgets.shortToast
import com.crashlytics.android.Crashlytics
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import io.reactivex.subjects.BehaviorSubject

class FetchDownloadListener(
    private val numberOfRequests: Int,
    private val reciterName: String,
    private val reciterIdentifier: String,
    private val selectedAyat: List<Aya>,
    private val repository: Repository,
    private val context: Context,
    private val doOnCompleted: (FetchListener) -> Unit
) : AbstractFetchListener() {
    init {
        progressListener.onNext(0f)
    }

    private var completedUriArray = arrayOfNulls<Uri>(numberOfRequests)
    private var completedDownloads = 0

    override fun onCompleted(download: Download) {
        completedDownloads++
        progressListener.onNext(completedDownloads.toFloat() / numberOfRequests * 100f)
        if (numberOfRequests >= completedDownloads) {
            val fileUri = download.fileUri
            completedUriArray[completedDownloads - 1] = fileUri

            val fileName = download.fileUri.toFile().nameWithoutExtension.toInt()

            val aya = selectedAyat.first { it.number == fileName }

            val reciter = Reciter(aya, reciterIdentifier, reciterName, download.fileUri)
            repository.addDownloadedReciter(reciter)
        }
        if (numberOfRequests == completedDownloads) {
            doOnCompleted.invoke(this)
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        shortToast(Stringify(R.string.error_downloading, context))
        Crashlytics.log(Priority.Medium, "FetchError ", error.name)
        DownloadingFragment.playerDownloadingCancelled.onNext(true)
    }

    companion object {
        val progressListener: BehaviorSubject<Float> = BehaviorSubject.create()
    }

}