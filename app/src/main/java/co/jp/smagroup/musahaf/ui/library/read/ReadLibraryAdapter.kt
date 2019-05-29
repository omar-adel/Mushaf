package co.jp.smagroup.musahaf.ui.library.read;

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.model.ReadTranslation
import co.jp.smagroup.musahaf.ui.commen.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.more.SettingsPreferencesConstant
import co.jp.smagroup.musahaf.utils.TextActionUtil
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import co.jp.smagroup.musahaf.utils.whiteSpaceMagnifier
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.invisible
import com.codebox.lib.android.views.utils.visible
import com.codebox.lib.android.widgets.shortToast
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.item_library_read.view.*

/**
 * Created by ${User} on ${Date}
 */
class ReadLibraryAdapter(private val dataList: MutableList<ReadTranslation>, private val repository: Repository) :
    RecyclerView.Adapter<ReadLibraryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.item_library_read, parent)

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val isLastPosition = dataList.size - 1 == position
        holder.bindData(data, position, isLastPosition)
    }

    inner class ViewHolder(
        viewType: Int,
        parent: ViewGroup
    ) :
        RecyclerView.ViewHolder(parent.inflater(viewType)) {
        val appPreferences = AppPreferences()

        fun bindData(readTranslation: ReadTranslation, position: Int, isLastPosition: Boolean) {
            if (position == 0) itemView.updatePadding(top = dp(65))
            else itemView.updatePadding(top = 0)

            val isTranslationWithAya =
                appPreferences.getBoolean(SettingsPreferencesConstant.TranslationWithAyaKey, true)
            if (isTranslationWithAya)
                itemView.aya_text.text = whiteSpaceMagnifier(readTranslation.quranicText)
            itemView.translation_tafseer_text_library.text = readTranslation.translationText


            if (readTranslation.isBookmarked)
                itemView.aya_number_library.setTextColor(Colour(R.color.focusColor))
            else
                itemView.aya_number_library.setTextColor(Colour(R.color.colorSecondary))


            itemView.aya_number_library.text = readTranslation.numberInSurah.toString().toLocalizedNumber()

            itemView.item_read_library_root_view.onClick {
                createPopup(readTranslation, context as BaseActivity)
            }
            if (isLastPosition)
                itemView.divider_item_library.invisible()
            else
                itemView.divider_item_library.visible()
        }

        private fun createPopup(readTranslation: ReadTranslation, activity: BaseActivity) {
            val popupMenu = popupMenu {
                if (MusahafApplication.isDarkThemeEnabled)
                    style = R.style.Widget_MPM_Menu_Dark_DarkBackground
                section {
                    item {
                        label = Stringify(R.string.share, activity)
                        callback = {
                            val shareText = convertTranslationToShare(readTranslation, activity)
                            TextActionUtil.shareText(activity, shareText, R.string.share_translation)
                            dismissOnSelect = true
                        }
                    }
                    item {
                        val shareText = convertTranslationToShare(readTranslation, activity)
                        label = Stringify(R.string.copy, activity)
                        callback = {
                            TextActionUtil.copyToClipboard(activity, shareText)
                            shortToast(Stringify(R.string.text_copied, activity))
                            dismissOnSelect = true
                        }
                    }
                    item {
                        label = Stringify(R.string.save_in_bookmarks, activity)
                        callback = {
                            val oldDataIdx = dataList.indexOf(readTranslation)

                            val newData = readTranslation.copy(isBookmarked = !readTranslation.isBookmarked)
                            dataList[oldDataIdx] = newData

                            repository.updateBookmarkStatus(
                                readTranslation.ayaNumber,
                                readTranslation.editionInfo.identifier,
                                !readTranslation.isBookmarked
                            )

                            if (newData.isBookmarked) itemView.aya_number_library.setTextColor(Colour(R.color.focusColor))
                            else itemView.aya_number_library.setTextColor(Colour(R.color.colorSecondary))
                        }
                    }
                }
            }
            popupMenu.show(activity, itemView.translation_tafseer_text_library)
        }

        private fun convertTranslationToShare(readTranslation: ReadTranslation, context: Context): String {
            var shareText = ""
            val ayaNumber = "${context.getString(R.string.aya_number)}: ${readTranslation.numberInSurah}\n"
            val pageNumber = "${context.getString(R.string.page)}: ${readTranslation.page}\n"
            val surahName = "${readTranslation.surah.name}\n"
            val ayaInfo = ayaNumber + pageNumber + surahName
            val ayaText = "{ ${readTranslation.quranicText} }\n$ayaInfo\n"

            val translationText = """ "${readTranslation.translationText}" """ + "\n"
            val translationInfo = if (readTranslation.translationOrTafsir == Edition.Tafsir)
                "${Stringify(R.string.tafseer, context)}: ${readTranslation.editionInfo.name}\n"
            else "${Stringify(R.string.translation, context)}: ${readTranslation.editionInfo.name}\n"

            shareText += ayaText + translationText + translationInfo
            shareText += "via @${Stringify(R.string.app_name, context)} for Android"

            return shareText
        }

    }
}