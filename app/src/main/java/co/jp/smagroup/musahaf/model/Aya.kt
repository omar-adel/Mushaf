package co.jp.smagroup.musahaf.model

import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.serialization.Serializable

@Table(database = MusahafDatabase::class, allFields = true)
@Serializable
data class Aya(
    @PrimaryKey
    var number: Int = 0,
    @ForeignKey(saveForeignKeyModel = true)
    var surah: Surah? = null,
    var text: String = "",
    var numberInSurah: Int = 0,
    var juz: Int = 0,
    var page: Int = 0,
    var hizbQuarter: Int = 0,
    @PrimaryKey
    @ForeignKey(saveForeignKeyModel = true)
    var edition: Edition? = null,
    var isBookmarked: Boolean = false
) : BaseModel() {
    constructor(aya: Aya, edition: Edition) : this(
        number = aya.number,
        surah = aya.surah,
        text = aya.text,
        numberInSurah = aya.numberInSurah,
        juz = aya.juz,
        page = aya.page,
        hizbQuarter = aya.hizbQuarter,
        edition = edition
    )
}
