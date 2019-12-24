package hong.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class BaseException : RuntimeException() {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    object BadRequest : BaseException()

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    object Unauthorized : BaseException()

    @ResponseStatus(HttpStatus.NOT_FOUND)
    object NotFound : BaseException()
}