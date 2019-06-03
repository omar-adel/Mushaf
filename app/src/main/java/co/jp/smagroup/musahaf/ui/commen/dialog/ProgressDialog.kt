package co.jp.smagroup.musahaf.ui.commen.dialog

/**
 * Created by ${User} on ${Date}
 */

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.ui.commen.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.utils.extensions.observeOnMainThread
import com.codebox.kidslab.Framework.Views.CustomToast
import com.codebox.lib.android.views.listeners.onClick
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_progress.view.*
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.math.roundToInt

class ProgressDialog : DialogFragment() {
    lateinit var progressListener: ProgressListener
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val disposables: MutableList<Disposable> = mutableListOf()
    private var dialogView: View? = null
    @Inject
    lateinit var repository: Repository

    init {
        MusahafApplication.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        disposables += repository.loadingStream.observeOnMainThread {
            if (it > 0) {
                val progress = it / 30f
                dialogView?.loading_per!!.text = "${(progress * 100).roundToInt()}%"
                dialogView?.progress_circular?.progress = progress

            }
        }
        repository.errorStream.onNext("")
        disposables += repository.errorStream.filter { it != "" }.observeOnMainThread {
            activity?.let { context -> CustomToast.makeLong(context, it) }
            progressListener.onCancelled()
            dismiss()
        }
        coroutineScope.launch {
            val edition = progressListener.downloadManger()
            if (repository.isDownloaded(edition)) {
                progressListener.onSuccess()
                dismiss()
            }
        }
        dialogView?.progress_cancel_button!!.onClick {
            progressListener.onCancelled()
            activity?.let { CustomToast.makeShort(it, R.string.cancelled) }
            dismiss()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AppTheme_TransparentDialog)
        dialogView = View.inflate(context, R.layout.dialog_progress, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        builder.setCancelable(false)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                progressListener.onCancelled()
                activity?.let { CustomToast.makeShort(it, R.string.cancelled) }
            }
            return@setOnKeyListener false
        }

        return dialog
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.onEach { it.dispose() }
        job.cancelChildren()
        progressListener.onFinish()
        val baseActivity = (activity as BaseActivity)
        if (baseActivity.currentSystemVisibility)
            baseActivity.systemUiVisibility(true)
    }

    companion object {
        const val TAG = "Dialog-Progress"
    }

    interface ProgressListener {
        fun onSuccess() {}
        fun onCancelled() {}
        fun onFinish() {}
        suspend fun downloadManger(): String
    }
}