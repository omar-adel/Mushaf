package co.jp.smagroup.musahaf.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.commen.CacheMaker
import kotlinx.coroutines.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.list

class QuranViewModel(private val repository: Repository) : ViewModel() {
    @UnstableDefault
    private val cacheMaker = CacheMaker()
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var _mainMusahaf: MutableLiveData<List<Aya>>
    val mainMusahaf: LiveData<List<Aya>>
        get() = _getMainMusahaf()

    fun prepareMainMusahaf() {
        if (!::_mainMusahaf.isInitialized)
            _mainMusahaf = MutableLiveData()
        coroutineScope.launch {
            withContext(Dispatchers.IO) { loadAyatData() }
            _mainMusahaf.postValue(QuranDataList)
        }
    }


    @UseExperimental(UnstableDefault::class)
    private suspend fun loadAyatData() {
        if (QuranDataList.isEmpty()) {
            val cachedData = cacheMaker.getSavedList(MusahafConstants.MainMusahaf, Aya.serializer().list)
            if (cachedData != null) QuranDataList = cachedData
            else {
                QuranDataList = repository.getMusahafAyat(MusahafConstants.MainMusahaf)
                saveAyatArrayToCache()
            }
        }
    }

    @UseExperimental(UnstableDefault::class)
    private suspend fun saveAyatArrayToCache() {
        cacheMaker.saveList(MusahafConstants.MainMusahaf, Aya.serializer().list, QuranDataList)
    }


    fun updateBookmarkStateInData(aya: Aya) {
        val index = QuranDataList.indexOf(aya)
        aya.isBookmarked = !aya.isBookmarked
        val newDatList = QuranDataList.toMutableList()
        newDatList[index] = aya
        QuranDataList = newDatList
        coroutineScope.launch { saveAyatArrayToCache() }
    }


    @Suppress("FunctionName")
    private fun _getMainMusahaf(): LiveData<List<Aya>> {
        if (!::_mainMusahaf.isInitialized)
            _mainMusahaf = MutableLiveData()
        return _mainMusahaf
    }

    override fun onCleared() {
        super.onCleared()
        job.cancelChildren()
    }

    companion object {
        var QuranDataList = listOf<Aya>()
            private set

        fun isQuranDataLoaded() = QuranDataList.isNotEmpty()
    }
}

