package co.jp.smagroup.musahaf.ui.library;

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.ui.library.read.ReadLibraryActivity
import co.jp.smagroup.musahaf.utils.extensions.onClicks
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_library.view.*

/**
 * Created by ${User} on ${Date}
 */
class LibraryAdapter(private val dataList: List<Edition>) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_library)
        return ViewHolder(v)
    }
    
    override fun getItemCount(): Int {
        return dataList.size
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        //val mContext = holder.itemView.context
        
        holder.bindData(data)
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        @SuppressLint("SetTextI18n")
        fun bindData(edition: Edition) {
            itemView.type_library.text = edition.type
            itemView.edition_name_library.text = edition.name

            onClicks(itemView.read_translation_button, itemView) {
                onItemClick(edition, itemView.context)
            }
        }
        
        private fun onItemClick(edition: Edition, context: Context) {
            val intent = context.newIntent<ReadLibraryActivity>()
            val bundle = bundleOf(ReadLibraryActivity.EditionIdKey to edition.identifier)
            
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
        
    }
    
    
}