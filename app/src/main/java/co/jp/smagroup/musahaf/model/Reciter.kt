package co.jp.smagroup.musahaf.model

import android.net.Uri
import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import co.jp.smagroup.musahaf.framework.database.UriConverter
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel


@Table(database = MusahafDatabase::class, allFields = true)
data class Reciter(
    @PrimaryKey var number: Int = 0,
    @PrimaryKey var identifier: String = "",
    @ForeignKey var surah: Surah? = null,
    @Column(typeConverter = UriConverter::class)
    var uri: Uri? = null,
    var name: String = "",
    var numberInSurah: Int = 0,
    var juz: Int = 0,
    var page: Int = 0
) : BaseModel() {
    constructor(aya: Aya, reciterIdentifier: String, reciterName: String, uri: Uri) : this(
        number = aya.number,
        identifier = reciterIdentifier,
        surah = aya.surah,
        page = aya.page,
        juz = aya.juz,
        numberInSurah = aya.numberInSurah,
        name = reciterName,
        uri = uri
    )
}