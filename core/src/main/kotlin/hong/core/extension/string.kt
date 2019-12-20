package hong.core.extension

import java.util.*

fun String.uuid(): UUID? = try {
    UUID.fromString(this)
} catch (e: IllegalArgumentException) {
    null
}

fun String.truncate(length: Int, truncateString: String = Typography.ellipsis.toString()): String =
    if (this.length <= length) {
        this
    } else {
        this.substring(0, length) + truncateString
    }