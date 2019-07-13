package co.jp.smagroup.musahaf.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Created by ${User} on ${Date}
 */

@UseExperimental(ExperimentalContracts::class)
val Any?.notNull: Boolean
    get() = this != null

val Any?.isNull: Boolean
    get() = this == null