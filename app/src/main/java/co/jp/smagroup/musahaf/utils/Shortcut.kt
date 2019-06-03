package co.jp.smagroup.musahaf.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.ShortcutDetails
import co.jp.smagroup.musahaf.ui.library.read.ReadLibraryActivity
import com.codebox.kidslab.Framework.Views.CustomToast
import com.codebox.lib.android.utils.AppPreferences
import java.util.*

/**
 * Created by ${User} on ${Date}
 */
object Shortcut{
    fun create(context: Context, shortcutDetails: ShortcutDetails, shortcutIntent: Intent) {
        // code for adding shortcut on pre oreo device
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            val shortcutIcon = Intent.ShortcutIconResource.fromContext(context, shortcutDetails.icon)
            val addIntent = Intent()
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutDetails.label)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcon)
            addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            context.sendBroadcast(addIntent)
            CustomToast.makeShort(context, R.string.shortcut_created)
        } else
            createDynamicShortcut(context,shortcutDetails,shortcutIntent,true)

    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun createDynamicShortcut(
        context: Context,
        shortcutDetails: ShortcutDetails,
        shortcutIntent: Intent,
        sendToHomeScreen: Boolean = false
    ) {

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
        val shortcut = ShortcutInfo.Builder(context, shortcutDetails.id)
            .setIntent(shortcutIntent)
            .setIcon(Icon.createWithResource(context, shortcutDetails.icon))
            .setShortLabel(shortcutDetails.label)
            .build()
        if (shortcutManager.dynamicShortcuts.size > 3) {
            val lastShortcut = shortcutManager.dynamicShortcuts.first { it.rank == 0 }
            shortcutManager.removeDynamicShortcuts(Arrays.asList(lastShortcut.id))
            shortcutManager.dynamicShortcuts
        }
        if (sendToHomeScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shortcutManager.isRequestPinShortcutSupported)
            shortcutManager.requestPinShortcut(shortcut, null)
        shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut))
    }
}
/*fun a(context: Context){
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
        val shortcutManager: ShortcutManager? = context.getSystemService(ShortcutManager::class.java)

        val shortcut: ShortcutInfo = ShortcutInfo.Builder(context, "second_shortcut")
                .setShortLabel(getString(R.string.str_shortcut_two))
                .setLongLabel(getString(R.string.str_shortcut_two_desc))
                .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                .setIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.co.in")
                    )
                )
                .build()
            shortcutManager!!.dynamicShortcuts = Arrays.asList(shortcut)
    }
}*/

