package co.jp.smagroup.musahaf.ui.quran.read.reciter

import android.widget.TextView
import androidx.core.text.toSpannable
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.commen.Priority
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.quran.read.ReadQuranActivity
import co.jp.smagroup.musahaf.utils.clearHighlighted
import co.jp.smagroup.musahaf.utils.highlightText
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import co.jp.smagroup.musahaf.utils.whiteSpaceMagnifier
import com.codebox.kidslab.Framework.Views.CustomToast
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.resoures.Stringer
import com.codebox.lib.android.widgets.shortToast
import com.crashlytics.android.Crashlytics
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_read_quran.*

/**
 * Created by ${User} on ${Date}
 */
class ExoPlayerListener(
    private val readQuranActivity: ReadQuranActivity,
    private val currentPlayedAyat: List<Aya>?,
    private val exoPlayer: ExoPlayer
) : Player.EventListener {
    @Suppress("DEPRECATION")
    private val basmalia = Stringer(R.string.basmalia)
    private val highlightedColor = Colour(R.color.colorSecondary, readQuranActivity)
    private val quranViewpager = readQuranActivity.quranViewpager
    override fun onPositionDiscontinuity(reason: Int) {
        val p = exoPlayer.currentWindowIndex
        val aya = currentPlayedAyat?.get(p)
        if (aya != null) {
            highlightCurrentPlayedAya(aya)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val exoPlayerErrorTag = "exoPlayerErrorTag"
        CustomToast.makeShort(readQuranActivity,R.string.playing_error)

        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> Crashlytics.log(
                Priority.Heigh,
                "TYPE_SOURCE: $exoPlayerErrorTag",
                error.message
            )
            ExoPlaybackException.TYPE_RENDERER -> Crashlytics.log(
                Priority.Heigh,
                "TYPE_RENDERER: $exoPlayerErrorTag",
                error.message
            )
            ExoPlaybackException.TYPE_UNEXPECTED -> Crashlytics.log(
                Priority.Heigh,
                "TYPE_UNEXPECTED: $exoPlayerErrorTag",
                error.message
            )
        }

        terminatePlayer()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            //The player is able to immediately play from the current position. This means the player does actually play media when playWhenReady is true. If it is false the player is paused.
            Player.STATE_READY -> {
                val p = exoPlayer.currentWindowIndex
                readQuranActivity.updatePagerPadding(0)
                val aya = currentPlayedAyat?.get(p)

                if (aya != null) highlightCurrentPlayedAya(aya)
            }
            //The player has finished playing the all passed media.
            Player.STATE_ENDED -> terminatePlayer()
        }
    }

    private var previousHighlightedText: TextView? = null
    private fun highlightCurrentPlayedAya(aya: Aya) {
        val currentPage = aya.page - 1
        if (quranViewpager.currentItem != currentPage)
            quranViewpager.currentItem = currentPage
        val mainTextView =
            quranViewpager.findViewWithTag<TextView>("surahPageText${quranViewpager.currentItem}")
        val removedBasmalia =
            if (aya.surah!!.englishName != MusahafConstants.Fatiha && aya.numberInSurah == 1) aya.text.replace(
                basmalia,
                ""
            )
            else aya.text
        val ayaFormatted = "${whiteSpaceMagnifier(removedBasmalia)} ${aya.numberInSurah.toString().toLocalizedNumber()}"

        if (mainTextView != null) {
            previousHighlightedText?.text?.toSpannable()?.clearHighlighted()
            previousHighlightedText = mainTextView
            highlightMainTextView(mainTextView, ayaFormatted)
        } else highlightMultiSurahText(ayaFormatted)
    }

    private fun highlightMainTextView(mainTextView: TextView, ayaFormatted: String) {
        previousHighlightedText = mainTextView
        val currentPlayedAyaStart = mainTextView.text.indexOf(ayaFormatted)
        val currentPlayedAyaEnd = currentPlayedAyaStart + ayaFormatted.length

        val spannable = mainTextView.text.toSpannable()
        spannable.clearHighlighted(0, spannable.length)
        spannable.highlightText(highlightedColor, currentPlayedAyaStart, currentPlayedAyaEnd)
    }

    private fun highlightMultiSurahText(ayaFormatted: String) {
        var currentPlayedTextView: TextView? = null
        var currentAyaIdx = -1
        for (textNumber in 0..4) {
            val textView: TextView? =
                quranViewpager.findViewWithTag("surahText$textNumber${quranViewpager.currentItem}")
            currentAyaIdx = textView?.text?.indexOf(ayaFormatted) ?: -1
            if (currentAyaIdx != -1) {
                currentPlayedTextView = textView
                break
            }
        }
        if (currentAyaIdx == -1)
            CustomToast.makeShort(readQuranActivity,"Error text not found")
        val spannable = currentPlayedTextView!!.text.toSpannable()
        spannable.clearHighlighted()
        spannable.highlightText(highlightedColor, currentAyaIdx, currentAyaIdx + ayaFormatted.length)
    }

    fun clearAllHighlighted() {
        val mainTextView = quranViewpager.findViewWithTag<TextView?>("surahPageText${quranViewpager.currentItem}")
        if (mainTextView != null)
            mainTextView.text.toSpannable().clearHighlighted()
        else
            for (textNumber in 0..4) {
                val textView: TextView? =
                    quranViewpager.findViewWithTag("surahText$textNumber${quranViewpager.currentItem}")
                textView?.text?.toSpannable()?.clearHighlighted()
            }
    }

    private fun terminatePlayer() {
        exoPlayer.removeListener(this)
        clearAllHighlighted()
        readQuranActivity.playerView.hide()
    }
}