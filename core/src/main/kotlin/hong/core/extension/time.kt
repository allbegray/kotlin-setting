package hong.core.extension

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

fun LocalDateTime.isWeekday(): Boolean = isWeekend().not()
fun LocalDateTime.isWeekend(): Boolean = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
fun LocalDate.lastDayOfMonth(): Int = this.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth