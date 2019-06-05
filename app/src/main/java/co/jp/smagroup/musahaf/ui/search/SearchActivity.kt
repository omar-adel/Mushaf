package co.jp.smagroup.musahaf.ui.search

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import androidx.core.view.isVisible
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.framework.utils.TextTypeOpt
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.ui.commen.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.utils.extensions.checked
import co.jp.smagroup.musahaf.utils.extensions.unChecked
import co.jp.smagroup.musahaf.utils.removeAllPunctuation
import com.codebox.kidslab.Framework.Views.CustomToast
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import com.codebox.lib.standard.stringsUtils.match
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.*
import javax.inject.Inject

class SearchActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {


    @Inject
    lateinit var repository: Repository

    @TextTypeOpt
    private var searchType: String = Edition.Quran

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private var quranWithoutTashkil = mutableListOf<Aya>()

    init {
        MusahafApplication.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initGroupChipListener()
        initSearch()
        back_button_search.onClick { finish() }
        coroutineScope.launch(Dispatchers.IO) {
            for (index in 0 until QuranViewModel.QuranDataList.size) {
                val aya = QuranViewModel.QuranDataList[index]
                quranWithoutTashkil.add(aya.copy(text = aya.text.removeAllPunctuation()))
            }
        }
    }


    private fun initSearch() {
        search_text_input.setOnEditorActionListener { _, actionId, _ ->
            empty_data_text.gone()
            runBlocking {
                coroutineScope.launch {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH && !search_text_input.text.isNullOrBlank() && !loading_search_result.isVisible) {
                        loading_search_result.visible()
                        val searchQuery = search_text_input.text.toString()
                        when (searchType) {
                            Edition.Quran -> {
                                if (quranWithoutTashkil.isNotEmpty()) {
                                    val searchResult =
                                        withContext(Dispatchers.IO) {
                                            quranWithoutTashkil.filter {
                                                it.text.match(searchQuery)
                                            }
                                        }
                                    dispatchSearchResult(searchResult, searchQuery)
                                } else
                                    CustomToast.makeShort(this@SearchActivity, R.string.wait)
                            }
                            else -> {
                                val searchResult = withContext(Dispatchers.IO) {
                                    repository.searchTranslation(
                                        searchQuery,
                                        searchType
                                    )
                                }
                                dispatchSearchResult(searchResult, searchQuery)
                            }
                        }
                    } else if (loading_search_result.isVisible)
                        CustomToast.makeShort(this@SearchActivity, R.string.wait)
                    else
                        CustomToast.makeShort(this@SearchActivity, R.string.empty_search_query)
                }
            }
            true
        }
    }

    private fun dispatchSearchResult(searchResult: List<Aya>, searchQuery: String) {
        loading_search_result.gone()
        number_of_result_search.visible()
        number_of_result_search.text =
            " ${searchResult.size} " + getString(R.string.result_for) + "  '$searchQuery'  "
        if (searchResult.isNotEmpty()) {
            empty_data_text.gone()

            recycler_search.adapter = SearchAdapter(searchResult, searchType)
        } else {
            empty_data_text.visible()
            recycler_search.adapter = null
        }
    }


    private fun initGroupChipListener() {
        search_quran_chip.setOnCheckedChangeListener(this)
        search_tafseer_chip.setOnCheckedChangeListener(this)
        search_translation_chip.setOnCheckedChangeListener(this)

        search_quran_chip.isChecked = true
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val elevation = resources!!.getDimension(R.dimen.item_elevation)
        val arrayOfChip = arrayOf(search_quran_chip, search_tafseer_chip, search_translation_chip)
        for (chip in arrayOfChip) {

            if (chip.id == buttonView.id) chip.checked(elevation)
            else chip.unChecked()

            searchType = when (buttonView.id) {
                R.id.search_quran_chip -> Edition.Quran
                R.id.search_tafseer_chip -> Edition.Tafsir
                else -> Edition.Translation
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancelChildren()
    }
}
