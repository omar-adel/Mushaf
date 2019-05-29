package co.jp.smagroup.musahaf.ui.quran.read.helpers

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.toSpannable
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import co.jp.smagroup.musahaf.utils.ClickableImageSpan
import co.jp.smagroup.musahaf.utils.clearHighlighted
import co.jp.smagroup.musahaf.utils.textdrawable.TextDrawable
import com.codebox.lib.android.utils.screenHelpers.dp

/**
 * Created by ${User} on ${Date}
 */
class FunctionalQuranText(private val context: Context, private val popupActions: PopupActions) {
    private val ayaNumberColor = if (MusahafApplication.isDarkThemeEnabled) Color.WHITE else Color.BLACK
    private var clickedStartSpanPosition = 0
    private var clickedEndSpanPosition = 0

    fun getQuranDecoratedText(
        str: String,
        previousAyaLength: Int,
        aya: Aya
    ): SpannableString {
        var bookmarkState = aya.isBookmarked
        val ayaNumberInSurah = aya.numberInSurah.toString().toLocalizedNumber()
        val text = SpannableString(str)
        val startChar = text.length - ayaNumberInSurah.length - 1
        val endChar = text.length - 1

        val span = getAyaImageNumber(aya.numberInSurah, bookmarkState)
        text.setSpan(span, startChar, endChar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        text.setSpan(object : ClickableImageSpan() {
            override fun onClick(widget: TextView, x: Int, y: Int) {

                highlightSelection(widget, text, previousAyaLength)

                popupActions.show(x, y, aya) { bookmarkStateChanged ->
                    //Clear Highlighted on touch out side popup.
                    val clickedTextSpan = widget.text.toSpannable()
                    if (bookmarkStateChanged) {
                        bookmarkState = if (bookmarkStateChanged) !bookmarkState else bookmarkState
                        val newSpan = getAyaImageNumber(aya.numberInSurah, bookmarkState)

                        clickedTextSpan.setSpan(
                            newSpan,
                            previousAyaLength + startChar,
                            previousAyaLength + endChar,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    clearHighlighted(clickedTextSpan, true)
                }

            }
        }, startChar, endChar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //for image clickable
        return text
    }

    private val highlightedColor = Color.parseColor("#73546E7A")
    private fun highlightSelection(view: TextView, text: CharSequence, startAtIndex: Int) {
        val spannable = view.text.toSpannable()
        //On outside popup click clear selection.
        //Clear previous selection.
        clearHighlighted(spannable, false)
        clickedEndSpanPosition = startAtIndex + text.length - 1
        clickedStartSpanPosition = startAtIndex
        spannable.setSpan(
            BackgroundColorSpan(highlightedColor),
            clickedStartSpanPosition,
            clickedEndSpanPosition,
            0
        )
    }

    private fun clearHighlighted(spannable: Spannable, clearAll: Boolean) {
        if (clearAll) {
            clickedStartSpanPosition = 0
            clickedEndSpanPosition = spannable.length - 1
        }
        spannable.clearHighlighted(clickedStartSpanPosition, clickedEndSpanPosition)
    }

    private fun getAyaImageNumber(numberInSurah: Int, isBookmarked: Boolean): ImageSpan {

        val ayaDecorImg = if (isBookmarked) R.drawable.ic_aya_number_bookmarked else R.drawable.ic_aya_number

        val endAyaImage = TextDrawable.builder()
            .beginConfig().fontSize(dp(16)).textColor(ayaNumberColor).bold().endConfig()
            .buildTextVectorImage(context, numberInSurah.toString().toLocalizedNumber(), ayaDecorImg, dp(38), dp(38))

        return ImageSpan(context, endAyaImage.toBitmap(), ImageSpan.ALIGN_BOTTOM)
    }
}