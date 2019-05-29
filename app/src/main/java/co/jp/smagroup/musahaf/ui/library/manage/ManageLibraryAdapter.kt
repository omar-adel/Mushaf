package co.jp.smagroup.musahaf.ui.library.manage;

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.DownloadingState
import co.jp.smagroup.musahaf.model.Edition
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_library_manage.view.*
import kotlinx.android.synthetic.main.item_library_manage_lang.view.*
import java.util.*

/**
 * Created by ${User} on ${Date}
 */
class ManageLibraryAdapter(
    private val dataList: List<Pair<Edition, DownloadingState>>,
    private val doOnItemClicked: (editionIdentifier: String, downloadState: DownloadingState) -> Unit

) : RecyclerView.Adapter<ManageLibraryAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent, viewType)
    
    override fun getItemCount(): Int = dataList.size
    
    override fun getItemViewType(position: Int): Int {
        val currentData = dataList[position]
        val prvData = if (position != 0) dataList[position - 1] else dataList[position]
        val prvLanguage = prvData.first.language
        
        return when {
            position == 0 -> itemLibraryWithLanguage
            currentData.first.language != prvLanguage -> itemLibraryWithLanguage
            else -> itemLibrary
        }
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        //val mContext = holder.itemView.context
        holder.bindData(data = data)
    }
    
    inner class ViewHolder(itemView: ViewGroup, @LayoutRes private val viewType: Int) :
        RecyclerView.ViewHolder(itemView.inflater(viewType)) {
        
        fun bindData(data: Pair<Edition, DownloadingState>) {
            if (viewType == itemLibraryWithLanguage)
                bindLibraryLanguageItem(data)
            else
                bindLibraryItem(data)
        }
        
        private fun bindLibraryLanguageItem(data: Pair<Edition, DownloadingState>) {
            itemView.language.text = data.first.language.capitalize()
            bindLibraryItem(data)
        }
        
        @SuppressLint("SetTextI18n")
        private fun bindLibraryItem(data: Pair<Edition, DownloadingState>) {
            itemView.edition_name.text = data.first.name
            itemView.download_info.text = "${data.second.stopPoint ?: 0} juz' downloaded"
            
            val icon = if (data.second.isDownloadCompleted) R.drawable.ic_check else R.drawable.ic_cloud_download
            itemView.download_img.setImageResource(icon)
            itemView.itemLibrary_rootView.onClick {
                doOnItemClicked.invoke(data.first.identifier, data.second)
            }
        }
        
    }
    
    companion object {
        private const val itemLibraryWithLanguage = R.layout.item_library_manage_lang
        private const val itemLibrary = R.layout.item_library_manage
    }
    
}