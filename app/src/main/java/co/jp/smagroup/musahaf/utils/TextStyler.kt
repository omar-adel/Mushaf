package com.codebox.physicstools.Framework.AndroidUtils

import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.widget.EditText
import co.jp.smagroup.musahaf.R
import com.codebox.lib.android.resoures.Colour


fun EditText.styler(Style: Int) {

    val styleSpans: Array<StyleSpan> = text.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)

    for (i in 0 until styleSpans.count()) {

        if (styleSpans[i].style == Style) {
            text.removeSpan(BackgroundColorSpan::class.java)
            return
        }
    }
    // If the selected text-part already has BOLD style on it, then
    // we need to disable it
    // Else we set BOLD style on it
    text.setSpan(
        StyleSpan(Style), selectionStart, selectionEnd,
        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
    )

}

fun EditText.textHighlighter() {
    val styleSpans: Array<out BackgroundColorSpan> =
        text.getSpans(selectionStart, selectionEnd, BackgroundColorSpan::class.java)
    val highlightingColor = Colour(R.color.yellow_400)
    for (i in 0 until styleSpans.count()) {
        if (styleSpans[i].backgroundColor == highlightingColor) {
            text.removeSpan(styleSpans[i])
            return
        }
    }
// setting blue background color

// setSpan requires start and end index
// in our case, it'Liked_Tag 0 and 5
// You can directly set fgSpan or bgSpan, however,
// to reuse defined CharacterStyle, use CharacterStyle.wrap()
    text.setSpan(
        BackgroundColorSpan(highlightingColor), selectionStart, selectionEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}


fun Spannable.removeTextStyle() {

    val spansToRemove = this.getSpans(0, length, Any::class.java)
    for (span in spansToRemove) {
        if (span is CharacterStyle)
            this.removeSpan(span)
    }
}
/*

private fun highlightString(input: String) {
    //Get the text from text view and create a spannable string
    val spannableString = SpannableString(mTextView.getText())
    //Get the previous spans and remove them
    val backgroundSpans = spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)

    for (span in backgroundSpans) {
        spannableString.removeSpan(span)
    }

    //Search for all occurrences of the keyword in the string
    var indexOfKeyword = spannableString.toString().indexOf(input)

    while (indexOfKeyword > 0) {
        //Create a background color span on the keyword
        spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + input.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //Get the next index of the keyword
        indexOfKeyword = spannableString.toString().indexOf(input, indexOfKeyword + input.length)
    }

    //Set the final text on TextView
    mTextView.setText(spannableString)
}*/
