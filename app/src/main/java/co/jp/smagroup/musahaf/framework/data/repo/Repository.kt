package co.jp.smagroup.musahaf.framework.data.repo

import android.util.Log
import co.jp.smagroup.musahaf.framework.api.QuranCloudAPI
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.local.LocalDataSource
import co.jp.smagroup.musahaf.framework.utils.NewAPI
import co.jp.smagroup.musahaf.framework.utils.ReciterRequestGenerator
import co.jp.smagroup.musahaf.framework.utils.TextTypeOpt
import co.jp.smagroup.musahaf.model.*
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.utils.extensions.onComplete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */
class Repository : RepositoryProviders {
    private val localDataSource = LocalDataSource()
    var errorStream: BehaviorSubject<String> = BehaviorSubject.create()
        private set
    var loadingStream: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)
        private set
    @Inject
    lateinit var quranCloudAPI: QuranCloudAPI

    init {
        MusahafApplication.appComponent.inject(this)
    }

    override suspend fun getSupportedLanguage(): List<String> {
        var languages = localDataSource.getSupportedLanguage()
        if (languages.isEmpty()) {
            quranCloudAPI.getSupportedLanguage().awaitRequest {
                localDataSource.addSupportedLanguages(it.languages)
                languages = it.languages
            }
        }
        return languages
    }

    override suspend fun getAyat(identifier: String): MutableList<Aya> {
        val downloadingState = localDataSource.getDownloadingState(identifier)
        val startingPoint: Int
        when {
            !downloadingState.isDownloadCompleted -> startingPoint = downloadingState.stopPoint ?: 1
            else -> return localDataSource.getAllAyatByIdentifier(identifier)
        }
        downloadAyat(identifier, startingPoint)

        val musahaf = localDataSource.getAllAyatByIdentifier(identifier)
        return if (musahaf.size == MusahafConstants.AyatNumber) musahaf else mutableListOf()
    }


    override suspend fun downloadAyat(identifier: String, startingPoint: Int) {
        var error: String? = null
        refreshForNewTask()
        loop@ for (juz in startingPoint..30) {
            quranCloudAPI.getQuran(juz, identifier).onComplete({
                localDataSource.addAyat(it.data)
                loadingStream.onNext(juz)
                if (juz < 30) {
                    localDataSource.addDownloadingState(DownloadingState(identifier, false, juz + 1))
                    serverDelay(juz)
                } else {
                    localDataSource.addDownloadingState(DownloadingState.downloadQuranTextCompleted(identifier))
                    refreshForNewTask()
                }
            }, onError = {
                error = it
                errorStream.onNext(it)
            })

            if (error != null)
                break@loop
        }
    }

    @NewAPI
    override suspend fun downloadFullDataReciter(fetch: Fetch, reciterId: String, reciterName: String) {
        val allReciterDownloads = localDataSource.getDownloadedDataReciter(reciterName)
        var notDownloadedAyaRequest = mutableListOf<Request>()

        if (allReciterDownloads.isNotEmpty()) {
            for (ayaNumber in 1..MusahafConstants.AyatNumber) if (allReciterDownloads.firstOrNull { it.number == ayaNumber } == null) {
                val aya = QuranViewModel.QuranDataList[ayaNumber - 1]
                val request =
                    ReciterRequestGenerator.createRequestFromFile(reciterName, reciterId, aya.surah!!, ayaNumber)
                notDownloadedAyaRequest.add(request)

            }
        } else {
            notDownloadedAyaRequest = QuranViewModel.QuranDataList.map {
                ReciterRequestGenerator.createRequestFromFile(
                    reciterName,
                    reciterId,
                    it.surah!!,
                    it.number
                )
            }.toMutableList()
        }

        fetch.enqueue(notDownloadedAyaRequest)
        /*  val downloadListener = FetchDownloadListener(
              notDownloadedAyaRequest.size,
              reciterName,
              QuranViewModel.QuranDataList,
              this
          ) {}
          fetch.addListener(downloadListener)
          Log.d("FullReciterDownloader: ", "Downloading started")*/
    }


    override fun updateBookmarkStatus(ayaNumber: Int, identifier: String, bookmarkStatus: Boolean) {
        localDataSource.updateBookmarkStatus(ayaNumber, identifier, bookmarkStatus)
    }

    override suspend fun searchTranslation(query: String, type: String): List<Aya> =
        localDataSource.searchTranslation(query, type).filter { isDownloaded(it.edition!!.identifier) }

    override suspend fun getMusahafAyat(identifier: String): MutableList<Aya> {
        val downloadingState = localDataSource.getDownloadingState(identifier)
        val startingPoint: Int
        when {
            !downloadingState.isDownloadCompleted -> {
                startingPoint = downloadingState.stopPoint ?: 1
                downloadAyat(identifier, startingPoint)
            }
            else -> return localDataSource.getAllAyatByIdentifier(identifier)
        }

        val musahaf = localDataSource.getAllAyatByIdentifier(identifier)
        return if (musahaf.size == MusahafConstants.AyatNumber) musahaf else mutableListOf()
    }

    override suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya> = localDataSource.getAyatByRange(from, to)


    override suspend fun getQuranBySurah(musahafIdentifier: String, surahNumber: Int): MutableList<Aya> =
        localDataSource.getQuranBySurah(musahafIdentifier, surahNumber)


    override suspend fun getAvailableEditions(format: String, language: String): List<Edition> {
        var editions = localDataSource.getAvailableEditions(format, language)
        if (editions.isEmpty())
            quranCloudAPI.getEditions(format, language).awaitRequest {
                localDataSource.addEditions(it.editions)
                editions = it.editions
            }
        return editions
    }

    override suspend fun getAllEditions(): List<Edition> {
        var editions = listOf<Edition>()
        refreshForNewTask()
        quranCloudAPI.getAllEditions().awaitRequest {
            editions = it.editions
        }
        return editions
    }

    override suspend fun getSurahs(): List<Surah> =
        SQLite.select().from(Surah::class.java).queryList()

    override suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya =
        localDataSource.getAyaByNumberInMusahaf(musahafIdentifier, number)

    override suspend fun getAllEditionsWithState(): List<Pair<Edition, DownloadingState>> {
        refreshForNewTask()
        var data = listOf<Pair<Edition, DownloadingState>>()
        quranCloudAPI.getAllEditions().awaitRequest {
            Log.d("getAllEditionsWithState", it.toString())
            data = it.editions.map { edition ->
                edition to localDataSource.getDownloadingState(edition.identifier)
            }
        }
        return data
    }

    override suspend fun getEditionsByType(type: String, fromInternet: Boolean): List<Edition> {
        var data = listOf<Edition>()
        if (!fromInternet)
            data = localDataSource.getEditionsByType(type)
        if (fromInternet || data.isEmpty())
            quranCloudAPI.getEditionsByType(type).awaitRequest {
                data = it.editions
                localDataSource.addEditions(data)
            }
        return data
    }

    override suspend fun getDownloadedEditions(): List<Edition> =
        localDataSource.getAllEditions().distinctBy { it.identifier }.filter { isDownloaded(it.identifier) }
            .filter { it.identifier != MusahafConstants.WordByWord && it.identifier != MusahafConstants.MainMusahaf }.sortedBy { it.language }


    override suspend fun isDownloaded(identifier: String): Boolean {
        val downloadingState = localDataSource.getDownloadingState(identifier)
        return downloadingState.isDownloadCompleted
    }

    override suspend fun getDownloadState(identifier: String): DownloadingState =
        localDataSource.getDownloadingState(identifier)


    override suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya> =
        localDataSource.getAllByBookmarkStatus(bookmarkStatus)

    override suspend fun getByAyaByBookmark(editionIdentifier: String, bookmarkStatus: Boolean): MutableList<Aya> =
        localDataSource.getByAyaByBookmark(editionIdentifier, bookmarkStatus)

    override suspend fun getPage(musahafIdentifier: String, page: Int): List<Aya>? =
        if (isDownloaded(musahafIdentifier)) localDataSource.getPage(musahafIdentifier, page)
        else null

    override suspend fun addDownloadReciter(data: List<Reciter>) {
        localDataSource.addDownloadReciter(data)
    }

    override fun addDownloadedReciter(reciter: Reciter) {
        localDataSource.addDownloadReciter(reciter)
    }

    override suspend fun getReciterDownload(ayaNumber: Int, reciterName: String): Reciter? =
        localDataSource.getReciterDownload(ayaNumber, reciterName)

    override fun getReciterDownloads(from: Int, to: Int, reciterIdentifier: String): List<Reciter> =
        localDataSource.getReciterDownloads(from, to, reciterIdentifier)

    private suspend fun serverDelay(juz: Int) {
        if (juz % 5 == 0)
            delay(7000)
        else
            delay(4000)
    }


    private suspend inline fun <reified T> Deferred<Response<T>>.awaitRequest(onSuccess: (T) -> Unit) {
        onComplete({
            onSuccess(it)
        }, onError = {
            errorStream.onNext(it)
        })
    }

    private fun refreshForNewTask() {
        errorStream.onNext("")
        loadingStream.onNext(0)
    }

}