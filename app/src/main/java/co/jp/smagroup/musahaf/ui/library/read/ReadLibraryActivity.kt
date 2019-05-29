package co.jp.smagroup.musahaf.ui.library.read

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.ReadTranslation
import co.jp.smagroup.musahaf.model.Surah
import co.jp.smagroup.musahaf.ui.commen.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.utils.RecyclerViewItemClickedListener
import co.jp.smagroup.musahaf.utils.extensions.onScroll
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.widgets.shortToast
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.activity_read_library.*
import kotlinx.android.synthetic.main.content_library_read.*
import kotlinx.coroutines.*
import javax.inject.Inject


class ReadLibraryActivity : BaseActivity() {
    @Inject
    lateinit var repository: Repository

    init {
        MusahafApplication.appComponent.inject(this)
    }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private var readSurahData: MutableList<ReadTranslation> = mutableListOf()
    private lateinit var readAdapter: ReadLibraryAdapter
    private var editionName = ""
    private var scrollPosition = 0
    private var preferences = AppPreferences()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            if (!MusahafApplication.isDarkThemeEnabled) window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            window.statusBarColor =
                if (MusahafApplication.isDarkThemeEnabled) Colour(R.color.color_background_dark) else Color.WHITE
        }

        setContentView(R.layout.activity_read_library)

        if (Build.VERSION.SDK_INT < 23) {
            toolbar_library_surah.setBackgroundColor(Colour(R.color.colorPrimary))
        }

        toolbar_library_surah.setNavigationIcon(if (MusahafApplication.isDarkThemeEnabled) R.drawable.ic_menu_light else R.drawable.ic_menu_dark)

        readAdapter = ReadLibraryAdapter(readSurahData, repository)

        val bundle = intent.extras
        //Extract the readSurahData…
        editionName = bundle?.getString(EditionIdKey) ?: throw Exception("No editionInfo found")
        scrollPosition = preferences.getInt(PreferencesConstants.LastScrollPosition + editionName, 0)
        val lastPageReadPage = preferences.getInt(PreferencesConstants.LastSurahViewed + editionName, 1)
        coroutineScope.launch { refreshReadingAdapter(lastPageReadPage) }
        initChooseSurahAdapter()

        setSupportActionBar(toolbar_library_surah)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        recycler_read_library.adapter = readAdapter
        val layoutManger = recycler_read_library.layoutManager as LinearLayoutManager
        recycler_read_library.onScroll { _, dy ->

            scrollPosition = layoutManger.findFirstCompletelyVisibleItemPosition()
            if (scrollPosition > 0)
                app_bar_library_surah.elevation = dp(5).toFloat()
            else
                app_bar_library_surah.elevation = dp(0).toFloat()

            if (dy > 0 && layoutManger.findFirstVisibleItemPosition() >= 2)
                hideToolbar()
            else if (dy < 0) showToolbar()


        }
    }

    private fun hideToolbar() {
        app_bar_library_surah
            .animate()
            .setDuration(250)
            .translationY(-app_bar_library_surah.height.toFloat())
            .start()
    }

    private fun showToolbar() {
        app_bar_library_surah
            .animate()
            .setDuration(400)
            .translationY(0f)
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancelChildren()
    }

    override fun onPause() {
        super.onPause()
        //if user changed configuration like rotation we save scroll position.
        saveCurrentReadScrollPosition(scrollPosition)
    }

    private fun saveCurrentReadScrollPosition(pos: Int) {
        preferences.put(PreferencesConstants.LastScrollPosition + editionName, pos)
        scrollPosition = pos
    }

    private fun initChooseSurahAdapter() {
        val onSurahChooseClickListener = object :
            RecyclerViewItemClickedListener<Surah> {
            override fun onItemClicked(dataItem: Surah, currentPosition: Int) {
                saveCurrentReadScrollPosition(0)
                drawer.closeDrawer(GravityCompat.START)
                coroutineScope.launch {
                    refreshReadingAdapter(dataItem.number)
                    preferences.put(PreferencesConstants.LastSurahViewed + editionName, dataItem.number)
                    showToolbar()
                }
            }
        }
        coroutineScope.launch {
            val surahsList = withContext(Dispatchers.IO) { repository.getSurahs() }
            recycler_choose_list.adapter = SurahChooseAdapter(surahsList, onSurahChooseClickListener)
        }
    }

    private suspend fun refreshReadingAdapter(surahNumber: Int) {
        val mainQuran: MutableList<Aya> = withContext(Dispatchers.IO) {
            repository.getQuranBySurah(
                MusahafConstants.MainMusahaf,
                surahNumber
            )
        }
        val translationQuran: MutableList<Aya> =
            withContext(Dispatchers.IO) { repository.getQuranBySurah(editionName, surahNumber) }
        Log.d("Quran GET", mainQuran.size.toString())

        val dataToAdd = mutableListOf<ReadTranslation>()
        for (index in mainQuran.indices) {
            val quran = mainQuran[index]
            val read =
                ReadTranslation(
                    quran,
                    quranicText = quran.text,
                    translationText = translationQuran[index].text,
                    isBookmarked = translationQuran[index].isBookmarked,
                    editionInfo = translationQuran[index].edition!!,
                    translationOrTafsir = translationQuran[index].edition!!.type
                )
            dataToAdd.add(read)
        }
        readSurahData.clear()
        readSurahData.addAll(dataToAdd)

        Log.d("Read GET", readSurahData.size.toString())
        readAdapter.notifyDataSetChanged()

        recycler_read_library.scrollToPosition(scrollPosition)

        toolbar_library_surah.title =
            if (isRightToLeft == 1) dataToAdd[0].surah.englishName else dataToAdd[0].surah.name

        if (isRightToLeft == 1) toolbar_library_surah.subtitle = dataToAdd[0].surah.englishNameTranslation

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.library_read_menu, menu)
        initFastScrollMenuItem(menu.findItem(R.id.fast_scroll_lib).actionView as ImageButton)
        return true
    }

    private fun initFastScrollMenuItem(fastScrollButton: ImageButton) {
        val outValue = TypedValue()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        } else theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        fastScrollButton.setBackgroundResource(outValue.resourceId)

        fastScrollButton.setImageResource(R.drawable.ic_move_to_page)
        fastScrollButton.setOnClickListener { createAyaNumberPopup(fastScrollButton) }
        fastScrollButton.setColorFilter(if (MusahafApplication.isDarkThemeEnabled) Color.WHITE else Color.BLACK)
    }

    private fun createAyaNumberPopup(view: View) {
        if (readSurahData.isNotEmpty()) {
            val popupMenu = popupMenu {
                if (MusahafApplication.isDarkThemeEnabled)
                    style = R.style.Widget_MPM_Menu_Dark_DarkBackground
                section {
                    title = getString(R.string.select_aya)
                    for (aya in readSurahData)
                        item {
                            label = aya.numberInSurah.toLocalizedNumber()
                            callback = {
                                recycler_read_library.scrollToPosition(aya.numberInSurah - 1)
                            }
                        }
                }
            }
            popupMenu.show(this, view)
        } else
            shortToast(getString(R.string.wait))
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) drawer.openDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }


    companion object {
        const val EditionIdKey = "Reading-Edition-Name"
    }
}
