package co.jp.smagroup.musahaf.model

/**
 * Created by ${User} on ${Date}
 */
data class Translation(
    val numberInMusahaf: Int,
    val selectedEditions: List<Edition>,
    val unSelectedEditions: List<Edition>
)