package hong.core.extension

fun <A, B> Pair<A, B>.swap(): Pair<B, A> = Pair(second, first)