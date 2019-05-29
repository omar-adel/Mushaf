@file:Suppress("MemberVisibilityCanBePrivate")

package co.jp.smagroup.musahaf.framework.database

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = MusahafDatabase.NAME, version = MusahafDatabase.VERSION)
object MusahafDatabase {
    const val NAME = "Musahaf"
    const val VERSION = 1
}