package co.jp.smagroup.musahaf.openSourceLicenses;

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_open_source_license.view.*

/**
 * Created by ${User} on ${Date}
 */
class OpenSourceLicenseAdapter(private val dataList: List<Pair<String, String>>) :
    RecyclerView.Adapter<OpenSourceLicenseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_open_source_license)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (libraryTitle, libraryLicense) = dataList[position]
        val mContext = holder.itemView.context
        holder.bindData(libraryTitle, libraryLicense)


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(libraryTitle: String, libraryLicense: String) {
            itemView.open_source_title.text = libraryTitle
            itemView.onClick {

            }
        }
    }


}