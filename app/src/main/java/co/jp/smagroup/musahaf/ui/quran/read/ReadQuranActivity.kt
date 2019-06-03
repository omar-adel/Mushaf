package co.jp.smagroup.musahaf.ui.quran.read

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.commen.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.commen.ViewModelFactory
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.ui.quran.read.reciter.DownloadingFragment
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ExoPlayerListener
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ReciterBottomSheet
import co.jp.smagroup.musahaf.utils.extensions.addOnPageSelectedListener
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import co.jp.smagroup.musahaf.utils.notNull
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import com.codebox.kidslab.Framework.Views.CustomToast
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.standard.lambda.unitFun
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read_quran.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.coroutines.*
import javax.inject.Inject


class ReadQuranActivity : BaseActivity(true), View.OnClickListener {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MusahafApplication.appComponent.inject(this)
    }

    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var exoPlayer: ExoPlayer? = null
    private var playerListener: ExoPlayerListener? = null
    private var playWhenReady = true
    private var doAfterPermission: unitFun? = null
    private var currentPageKey = "current read page"
    private lateinit var viewModel: QuranViewModel

    private var disposable: Disposable? = null
    private val job: Job = SupervisorJob()
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_quran)
        val bundle = intent.extras
        //Extract the data.
        var startAtPage = bundle?.getInt(START_AT_PAGE_KEY) ?: 1

        if (savedInstanceState != null) {
            startAtPage = savedInstanceState.getInt(currentPageKey, startAtPage)
        }
        viewModel = viewModelOf(QuranViewModel::class.java, viewModelFactory)

        viewModel.mainMusahaf.observer(this) {
            val data = it.groupBy { it.page }
            if (savedInstanceState == null) {
                val aya = data[startAtPage]!!.last()
                createHizbToast(aya)
            }

            initViewPager(startAtPage, data)
        }
        viewModel.prepareMainMusahaf()

        activateClickListener()
    }


    fun updateBookmarkState(aya: Aya) {
        repository.updateBookmarkStatus(aya.number, aya.edition!!.identifier, !aya.isBookmarked)
        viewModel.updateBookmarkStateInData(aya)
    }

    private fun initViewPager(
        startAtPage: Int,
        data: Map<Int, List<Aya>>
    ) {
        quranViewpager.adapter = ReadQuranPagerAdapter(this, data, coroutineScope)

        quranViewpager.setCurrentItem(startAtPage - 1, false)

        quranViewpager.addOnPageSelectedListener {
            val aya = data[it + 1]!!.last()
            createHizbToast(aya)
            if (playerView.isShown) updatePagerPadding(dp(80))
            else updatePagerPadding(0)
        }

    }

    private fun createHizbToast(aya: Aya) {

        val hizbQuarters = aya.hizbQuarter
        val integerNumberOfHizb = hizbQuarters / 4
        val floatingNumberOfHizb = hizbQuarters % 4

        val floatingText =
            if (integerNumberOfHizb != 0) "${integerNumberOfHizb.toLocalizedNumber()} ${floatingNumberOfHizb.toLocalizedNumber()}/" + 4.toLocalizedNumber()
            else "${floatingNumberOfHizb.toLocalizedNumber()}/" + 4.toLocalizedNumber()

        val toastText =
            "${getString(R.string.hizb)} " + if (floatingNumberOfHizb == 0) integerNumberOfHizb.toLocalizedNumber() else floatingText

        CustomToast.make(this, toastText, Toast.LENGTH_LONG)
    }

    private fun initializePlayer() {

        if (exoPlayer == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()

            exoPlayer = ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(adaptiveTrackSelectionFactory), DefaultLoadControl(), null,
                BANDWIDTH_METER
            )
            playerView.player = exoPlayer
            //if exoMediaSource not null then resume player with previous media source. This happens when activity configuration changed or resumed.
            if (exoMediaSource.notNull) resumePlayer()
            //resting saved position for the new media source.
            else {
                currentWindow = 0
                playbackPosition = 0
            }
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.seekTo(
                currentWindow,
                playbackPosition
            )
            playerListener = ExoPlayerListener(
                this,
                currentPlayedAyat,
                exoPlayer!!
            )
            exoPlayer!!.addListener(playerListener)

        }

    }

    private fun resumePlayer() {
        playerView.show()
        updatePagerPadding(dp(80))
        playPauseButton.setImageResource(R.drawable.ic_pause)
        exoPlayer!!.prepare(exoMediaSource, false, false)
    }

    fun releasePlayer() {
        exoPlayer?.let {
            playerView.hide()
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            it.removeListener(playerListener)
            exoPlayer = null
        }
    }

    fun setExoPLayerMediaSource(
        newMediaSource: MediaSource,
        selectedAyat: List<Aya>
    ) {
        currentPlayedAyat = selectedAyat

        if (newMediaSource.notNull) {
            //Resetting the exoMediaSource and re-instantiate exo-player.
            exoMediaSource = null
            releasePlayer()
            initializePlayer()
        }
        updatePagerPadding(dp(80))
        exoPlayer?.prepare(newMediaSource, true, true)
        playPauseButton.setImageResource(R.drawable.ic_pause)
        playerView.show()
        exoMediaSource = newMediaSource
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.playerSettings -> {
                val reciterBottomSheet = ReciterBottomSheet()
                reciterBottomSheet.isComingFromMediaPlayer = true
                reciterBottomSheet.show(supportFragmentManager, ReciterBottomSheet.TAG)
            }

            R.id.playPauseButton -> {
                exoPlayer?.let {
                    it.playWhenReady = !it.playWhenReady
                    if (it.playWhenReady) playPauseButton.setImageResource(R.drawable.ic_pause)
                    else playPauseButton.setImageResource(R.drawable.ic_play)
                }
            }

            R.id.stopPlayer -> {
                playerListener?.clearAllHighlighted()
                playerView.hide()
                releasePlayer()
                updatePagerPadding(0)
            }
        }
    }

    fun executeWithPermission(block: unitFun) {
        doAfterPermission = block
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result == PackageManager.PERMISSION_GRANTED)
                block.invoke()
            else
                requestForSpecificPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            block.invoke()
        }
    }

    private fun activateClickListener() {
        stopPlayer.setOnClickListener(this)
        playPauseButton.setOnClickListener(this)
        playerSettings.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23)
            initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        preferences.put(PreferencesConstants.LastSurahViewed, quranViewpager.currentItem)
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    private fun requestForSpecificPermission(vararg permissions: String) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doAfterPermission?.invoke()
            } else CustomToast.makeShort(this, "Cannot save audio files without the requested permission")
        }
    }


    override fun onResume() {
        super.onResume()
        systemUiVisibility(true)
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        job.cancelChildren()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exoMediaSource = null
        currentPlayedAyat = null
        DownloadingFragment.playerDownloadingCancelled.onNext(true)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(currentPageKey, quranViewpager.currentItem + 1)
    }

    fun updatePagerPadding(pad: Int) {
        val scrollView = quranViewpager.findViewWithTag<NestedScrollView>("pageScroller${quranViewpager.currentItem}")
        scrollView?.updatePadding(bottom = pad)
    }

    companion object {
        private var exoMediaSource: MediaSource? = null
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
        const val START_AT_PAGE_KEY = "start-at-page"
        private var currentPlayedAyat: List<Aya>? = null
        private var currentWindow = 0
        private var playbackPosition: Long = 0
    }

}
