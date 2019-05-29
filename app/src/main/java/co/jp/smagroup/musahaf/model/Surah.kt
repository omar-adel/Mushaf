package co.jp.smagroup.musahaf.model

import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.serialization.Serializable


@Serializable
@Table(database = MusahafDatabase::class, allFields = true)
data class Surah(
    @PrimaryKey var number: Int = 0,
    var name: String = "",
    var englishName: String = "",
    var englishNameTranslation: String = "",
    var revelationType: String = "",
    var numberOfAyahs: Int = 0
) : BaseModel()