package co.jp.smagroup.musahaf.utils.extensions

/**
 * Created by ${User} on ${Date}
 */
val IntRange.asIndices:IntRange
 get() {
     val startPoint = start
     val endPoint = endInclusive -1
     return startPoint..endPoint
 }