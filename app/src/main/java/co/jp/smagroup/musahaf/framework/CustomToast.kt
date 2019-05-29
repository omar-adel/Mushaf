package com.codebox.kidslab.Framework.Views

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.IntDef
import co.jp.smagroup.musahaf.R
import kotlinx.android.synthetic.main.layout_toast.view.*


class CustomToast {

    @IntDef( Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    companion object {
        fun make(context: Context,message: String, @Duration showingDuration: Int) {
            val layout = View.inflate(context, R.layout.layout_toast, null)

            layout.toast_text.text = message
            val toast = Toast(context)
            with(toast) {
                view = layout
                duration = showingDuration
                show()
            }
        }

    }
}
