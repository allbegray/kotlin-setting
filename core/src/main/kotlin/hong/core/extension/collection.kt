package hong.core.extension

fun <T> List<T>.isDuplicated(): Boolean = distinct().size != size

infix fun <T> Iterable<T>.skip(n: Int): List<T> = drop(n)

infix fun <T> Iterable<T>.limit(n: Int): List<T> = take(n)