package hong.core.extension

import java.time.DayOfWeek
import java.time.LocalDateTime

fun LocalDateTime.isWeekday(): Boolean = isWeekend().not()
fun LocalDateTime.isWeekend(): Boolean = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY