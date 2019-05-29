package co.jp.smagroup.musahaf.model

import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import com.google.gson.annotations.SerializedName
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.serialization.Serializable

@Serializable
@Table(database = MusahafDatabase::class, allFields = true)
data class Edition(
    @PrimaryKey
    var identifier: String = "",
    var language: String = "",
    var name: String = "",
    var englishName: String = "",
    var format: String = "",
    var type: String = ""
) : BaseModel() {

    companion object {
        const val Tafsir = "tafsir"
        const val Translation = "translation"
        const val Quran = "quran"
    }
}
