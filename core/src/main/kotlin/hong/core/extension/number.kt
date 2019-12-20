package hong.core.extension

import java.util.*

fun Int.uuid(): UUID = toLong().uuid()
fun Long.uuid(): UUID = UUID.fromString("00000000-0000-0000-0000-${"%012d".format(this)}")

fun Double.toFixed(digits: Int): String = String.format("%.${digits}f", this)
fun Float.toFixed(digits: Int): String = toDouble().toFixed(digits)