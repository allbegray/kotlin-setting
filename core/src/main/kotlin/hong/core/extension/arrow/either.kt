package hong.core.extension.arrow

import arrow.core.Either
import arrow.core.EitherOf
import arrow.core.flatMap

fun <A, B, C> EitherOf<A, B>.then(f: (B) -> Either<A, C>): Either<A, C> = flatMap(f)