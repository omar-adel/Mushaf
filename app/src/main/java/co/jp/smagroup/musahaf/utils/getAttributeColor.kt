package co.jp.smagroup.musahaf.utils

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

@ColorInt
fun getAttributeColor(context: Context, @AttrRes colorAttribute: Int): Int {
    val attrs = intArrayOf(colorAttribute)
    val ta = context.obtainStyledAttributes(attrs)
    /*Get the color resourceID that we want (the first index, and only item, in the
    attrs array). Use ContextCompat to get the color according to the theme.
    */
    @ColorInt val color = ContextCompat.getColor(context,
            ta.getResourceId(0, -1))
    // ALWAYS call recycle() on the TypedArray when you’re done.
    ta.recycle()
    return color
}