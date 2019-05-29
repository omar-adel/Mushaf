package co.jp.smagroup.musahaf.framework.data.local

import co.jp.smagroup.musahaf.framework.api.ApiModels
import co.jp.smagroup.musahaf.framework.utils.EditionTypeOpt
import co.jp.smagroup.musahaf.framework.utils.TextTypeOpt
import co.jp.smagroup.musahaf.framework.utils.TranslationTypeOpt
import co.jp.smagroup.musahaf.model.*

/**
 * Created by ${User} on ${Date}
 */
interface LocalDataSourceProviders {

    suspend fun addAyat(quran: ApiModels.QuranDataApi)
    suspend fun addSupportedLanguages(languages: List<String>)
    suspend fun addEditions(editions: List<Edition>)
    suspend fun addEdition(edition: Edition)
    suspend fun addDownloadingState(downloadingState: DownloadingState)
    suspend fun addDownloadReciter(reciters: List<Reciter>)
    fun addDownloadReciter(reciter: Reciter)

    fun updateBookmarkStatus(ayaNumber: Int,identifier: String, bookmarkStatus: Boolean)

    suspend fun getSupportedLanguage(): List<String>
    suspend fun getAvailableEditions(format: String, language: String): List<Edition>
    suspend fun getAllEditions(): List<Edition>
    fun getEditionsByType(@EditionTypeOpt type: String): List<Edition>
    suspend fun getDownloadingState(identifier: String): DownloadingState?
    suspend fun getDownloadedDataReciter(reciterName: String): List<Reciter>

    suspend fun getAllAyatByIdentifier(musahafIdentifier: String): MutableList<Aya>
    suspend fun getPage(musahafIdentifier: String, page: Int): MutableList<Aya>
    suspend fun getQuranBySurah(musahafIdentifier: String, surahNumber: Int): MutableList<Aya>
    suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya
    suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya>
    suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya>
    suspend fun getByAyaByBookmark(editionIdentifier: String,bookmarkStatus: Boolean): MutableList<Aya>
    suspend fun searchTranslation(query: String,  type: String): MutableList<Aya>


    suspend fun getReciterDownload(ayaNumber: Int, reciterIdentifier: String): Reciter?
    fun getReciterDownloads(from: Int, to: Int, reciterIdentifier: String): List<Reciter>


}