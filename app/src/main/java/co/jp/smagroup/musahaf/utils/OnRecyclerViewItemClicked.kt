package co.jp.smagroup.musahaf.utils

/**
 * Created by ${User} on ${Date}
 */
interface RecyclerViewItemClickedListener<T : Any> {
    fun onItemClicked(dataItem: T, currentPosition: Int)
}