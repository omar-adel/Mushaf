package co.jp.smagroup.musahaf.utils

import android.text.Spannable
import android.text.style.BackgroundColorSpan

fun whiteSpaceMagnifier(text: String): String =
    text.replace(" ", "   ")


fun Spannable.clearHighlighted(start: Int = 0, end: Int = length) {
    val styleSpans: Array<out BackgroundColorSpan> =
        getSpans(start, end, BackgroundColorSpan::class.java)
    for (style in styleSpans)
        removeSpan(style)
}

fun Spannable.highlightText(color: Int, start: Int, end: Int) {
    setSpan(BackgroundColorSpan(color), start, end, 0)
}