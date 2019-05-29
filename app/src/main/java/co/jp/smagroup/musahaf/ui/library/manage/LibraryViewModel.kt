package co.jp.smagroup.musahaf.ui.library.manage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.framework.utils.TextTypeOpt
import co.jp.smagroup.musahaf.model.DownloadingState
import co.jp.smagroup.musahaf.model.Edition
import kotlinx.coroutines.*


class LibraryViewModel(private val repository: Repository) : ViewModel() {
    private lateinit var _allAvailableEditions: MutableLiveData<List<Pair<Edition, DownloadingState>>>
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    val allAvailableEditions: LiveData<List<Pair<Edition, DownloadingState>>>
        get() = _getEditions()


    private fun _getEditions(): LiveData<List<Pair<Edition, DownloadingState>>> {
        if (!::_allAvailableEditions.isInitialized)
            _allAvailableEditions = MutableLiveData()
        return _allAvailableEditions
    }

    fun getEditions() = runBlocking {
        if (!::_allAvailableEditions.isInitialized)
            _allAvailableEditions = MutableLiveData()

        if (DataHolder.editionsData.isEmpty()) {
            coroutineScope.launch {
                DataHolder.editionsData = withContext(Dispatchers.IO) { repository.getAllEditionsWithState() }
                _allAvailableEditions.postValue(DataHolder.editionsData)
            }
        } else {
            _allAvailableEditions.postValue(DataHolder.editionsData)
        }
    }

    suspend fun downloadMusahaf(editionIdentifier: String, downloadingState: DownloadingState) {
        withContext(Dispatchers.IO) {
            repository.downloadAyat(
                editionIdentifier,
                downloadingState.stopPoint ?: 1)
        }
    }

    fun updateDataDownloadState(identity: String) {
        coroutineScope.launch {
            DataHolder.editionsData = withContext(Dispatchers.IO) {
                DataHolder.editionsData.map {
                    if (it.first.identifier == identity)
                        it.first to repository.getDownloadState(identity)
                    else it
                }
            }
            _allAvailableEditions.value = DataHolder.editionsData
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancelChildren()
        DataHolder.editionsData = emptyList()
    }
}

object DataHolder {
    var editionsData = listOf<Pair<Edition, DownloadingState>>()
}
