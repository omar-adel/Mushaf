package co.jp.smagroup.musahaf.ui.bookmarks;

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.library.read.ReadLibraryActivity
import co.jp.smagroup.musahaf.ui.quran.read.ReadQuranActivity
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_bookmark.view.*
import kotlinx.android.synthetic.main.item_bookmark_with_header.view.*

/**
 * Created by ${User} on ${Date}
 */
class BookmarksAdapter(
    private val dataList: List<Aya>,
    private val fragment: BookmarksFragment
) : RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(viewType, parent, fragment)

    override fun getItemViewType(position: Int): Int {
        val currentData = dataList[position]
        val prvData = if (position != 0) dataList[position - 1] else dataList[position]
        val prvType = prvData.edition!!.type

        return when {
            position == 0 -> itemWithHeader
            currentData.edition!!.type != prvType -> itemWithHeader
            else -> itemBookmark
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bindData(data)
    }


    class ViewHolder(
        @LayoutRes private val layoutId: Int, parent: ViewGroup,
        private val fragment: BookmarksFragment
    ) : RecyclerView.ViewHolder(parent.inflater(layoutId)) {

        private var preferences = AppPreferences()

        fun bindData(aya: Aya) {
            if (layoutId == itemWithHeader)
                itemView.bindItemWithHeader(aya)
            else itemView.bindItem(aya)
        }

        private fun View.bindItem(aya: Aya) {
            surah_name_bookmark.text = if (isRightToLeft == 1) aya.surah!!.englishName else aya.surah!!.name
            val page = context.getString(R.string.page) + " ${aya.page.toString().toLocalizedNumber()}"
            val juz = context.getString(R.string.juz) + " ${aya.juz.toString().toLocalizedNumber()}"
            val numberInSurah = context.getString(R.string.aya) + " ${aya.numberInSurah.toString().toLocalizedNumber()}"
            val editionName = if (aya.edition!!.type != Edition.Quran) aya.edition!!.name else ""
            info_bookmark.text = "$page, $juz, $numberInSurah, $editionName"

            item_bookmark_root_view.onClick {
                val bundle = Bundle()
                val intent: Intent
                if (aya.edition!!.type == Edition.Quran) {
                    intent = context.newIntent<ReadQuranActivity>()
                    bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, aya.page)
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                } else {
                    intent = context.newIntent<ReadLibraryActivity>()
                    val editionIdentifier = aya.edition!!.identifier
                    bundle.putString(ReadLibraryActivity.EditionIdKey, editionIdentifier)
                    intent.putExtras(bundle)

                    preferences.put(PreferencesConstants.LastScrollPosition + editionIdentifier, aya.numberInSurah - 1)
                    preferences.put(PreferencesConstants.LastSurahViewed + editionIdentifier, aya.surah!!.number)

                    context.startActivity(intent)
                }
            }
            remove_bookmark.onClick {
                fragment.removeBookmarked(aya)
            }
        }

        private fun View.bindItemWithHeader(aya: Aya) {
            type_header.text = when (aya.edition!!.type) {
                Edition.Quran -> context.getString(R.string.quran)
                Edition.Tafsir -> context.getString(R.string.tafseer)
                else -> context.getString(R.string.translation)
            }

            bindItem(aya)
        }

    }

    companion object {
        const val itemWithHeader = R.layout.item_bookmark_with_header
        const val itemBookmark = R.layout.item_bookmark
    }
}