package co.jp.smagroup.musahaf.ui.search

import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import kotlinx.serialization.Serializable

/**
 * Created by ${User} on ${Date}
 */
object Models {
    @Serializable
    data class SearchableQuran(val data: Data)
    @Serializable
    data class Data(val ayahs: List<Aya>, val edition: Edition)
}