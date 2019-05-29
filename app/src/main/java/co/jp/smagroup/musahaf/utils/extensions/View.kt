package co.jp.smagroup.musahaf.utils.extensions

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import com.codebox.lib.android.views.listeners.onClick
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.utils.OnPageSelectedListener
import com.codebox.lib.android.resoures.Colour
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

/**
 * Created by ${User} on ${Date}
 */


inline fun <T:View> onClicks(vararg views: T, crossinline block: T.() -> Unit) {
    for (view in views)
        view.onClick(block)
}


inline fun ViewPager.addOnPageSelectedListener(crossinline block: (position:Int) -> Unit){
    addOnPageChangeListener(object : OnPageSelectedListener {
        override fun onPageSelected(position: Int) { block.invoke(position) }
})

}

inline fun RecyclerView.onScroll(crossinline action: (dx: Int, dy: Int) -> Unit) {
    
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            action(dx, dy)
        }
    })
}

inline fun TextInputEditText.onSubmit(
    submitId: Int = EditorInfo.IME_ACTION_SEARCH,
    crossinline action: (searchKeyword: String) -> Unit
) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == submitId && !text.isNullOrBlank()) {

            action(text!!.toString())

            return@setOnEditorActionListener true
        }
        false
    }
}

fun Chip.unChecked() {
    setChipBackgroundColorResource(R.color.colorPrimary)
    setTextColor(Color.WHITE)
    putElevation(0f)
}

fun Chip.checked(elevation: Float) {
    setChipBackgroundColorResource(R.color.white)
    setTextColor(Color.BLACK)
    putElevation(elevation)
}
fun View.putElevation(value: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = value
    }
}