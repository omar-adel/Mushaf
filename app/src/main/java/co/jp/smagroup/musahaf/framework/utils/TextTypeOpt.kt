package co.jp.smagroup.musahaf.framework.utils

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import co.jp.smagroup.musahaf.model.Edition

/**
 * Created by ${User} on ${Date}
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION
)
@StringDef(Edition.Quran, Edition.Tafsir, Edition.Translation)
@Retention(AnnotationRetention.BINARY)
annotation class TextTypeOpt