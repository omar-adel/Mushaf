package co.jp.smagroup.musahaf.framework.database

import android.net.Uri
import com.raizlabs.android.dbflow.converter.TypeConverter


@com.raizlabs.android.dbflow.annotation.TypeConverter
class  UriConverter : TypeConverter<String,Uri>() {
    override fun getDBValue(model: Uri?): String =
        model.toString()

    override fun getModelValue(data: String?): Uri =
    Uri.parse(data)
}