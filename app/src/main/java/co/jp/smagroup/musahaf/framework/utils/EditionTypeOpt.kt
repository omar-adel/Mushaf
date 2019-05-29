package co.jp.smagroup.musahaf.framework.utils

import androidx.annotation.StringDef
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants

/**
 * Created by ${User} on ${Date}
 */
@Target(AnnotationTarget.VALUE_PARAMETER,AnnotationTarget.LOCAL_VARIABLE,AnnotationTarget.FIELD,AnnotationTarget.PROPERTY,AnnotationTarget.PROPERTY_GETTER,AnnotationTarget.FUNCTION)
@StringDef(MusahafConstants.Text, MusahafConstants.Audio)
@Retention(AnnotationRetention.BINARY)
annotation class EditionTypeOpt