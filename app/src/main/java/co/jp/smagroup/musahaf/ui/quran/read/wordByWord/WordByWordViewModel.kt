package co.jp.smagroup.musahaf.ui.quran.read.wordByWord

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by ${User} on ${Date}
 */
class WordByWordViewModel : ViewModel() {
    @Suppress("JoinDeclarationAndAssignment")
    private var wordByWordData: MutableLiveData<List<Pair<String?, String>>>
    
    init {
        wordByWordData = MutableLiveData()
    }
    
    fun setWordByWordData(value: List<Pair<String?, String>>) {
        wordByWordData.value = value
    }
    
    fun getWordByWord(): LiveData<List<Pair<String?, String>>> = wordByWordData
    
}