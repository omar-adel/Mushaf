package co.jp.smagroup.musahaf.ui.quran.read.wordByWord

import androidx.annotation.UiThread
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.utils.isPunctuation
import co.jp.smagroup.musahaf.utils.removePunctuation
import com.codebox.lib.standard.stringsUtils.match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by ${User} on ${Date}
 */
class WordByWordLoader(val repository: Repository) {
    
    @UiThread
    suspend inline fun getDataWordByWord(
        selectedData: ArrayList<String>,
        aya: Aya,
        onSuccess: (List<Pair<String?, String>>) -> Unit,
        onError: () -> Unit
    ) {
        val ayaResult = mutableListOf<Pair<String?, String>>()
        val wordByWordPageData =
            withContext(Dispatchers.IO) { repository.getPage(MusahafConstants.WordByWord, aya.page) }
        
        if (wordByWordPageData != null) {
            for (arabicWord in selectedData) {
                var wordDict = ""
                
                val result = wordByWordPageData.firstOrNull {
                    val wordByWordAya = it.text.removePunctuation()
                    val arabicNoPunctuation = arabicWord.removePunctuation()
                    val arabicWithHamza = arabicWord.removePunctuation("ٲ")
                    val arabicWithAlef = arabicWord.removePunctuation("ا")
                    
                    ((wordByWordAya.match(arabicNoPunctuation) ||
                            wordByWordAya.match(arabicWithHamza) ||
                            wordByWordAya.match(arabicWithAlef)) && it.surah!!.englishName == aya.surah!!.englishName)
                }
                if (result != null) {
                    val arabicWordNoPunctuation = arabicWord.removePunctuation()
                    val arabicWordWithAlef = arabicWord.removePunctuation("ٲ")
                    val correctedText = result.text.replace("ٮ", "ى")
                    for (index in 0 until correctedText.length) {
                        val currentChar = correctedText[index]
                        if (currentChar in 'A'..'Z' || currentChar in 'a'..'z')
                            continue
                        if (currentChar != '|' && currentChar != '$' && currentChar != ' ' && !currentChar.isDigit()) {
                            if (!currentChar.isPunctuation)
                                wordDict += currentChar
                        }
                        if (wordDict == arabicWordNoPunctuation || wordDict == arabicWordWithAlef) {
                            val rawData = correctedText.substring(index, correctedText.length - 1)
                            val englishWordAya = rawData.split('|', limit = 3)
                            ayaResult.add(englishWordAya[1] to arabicWord)
                            break
                        }
                        if (currentChar == '|') {
                            wordDict = ""
                        }
                    }
                } else
                    ayaResult.add(null to arabicWord)
            }
            onSuccess(ayaResult)
        } else
            onError()
    }
    
    
}