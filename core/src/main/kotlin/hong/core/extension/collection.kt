package hong.core.extension

import java.time.LocalDateTime

fun <T> List<T>.isDuplicated(): Boolean {
    return distinct().size != size
}

infix fun <T> Iterable<T>.skip(n: Int): List<T> = drop(n)

infix fun <T> Iterable<T>.limit(n: Int): List<T> = take(n)