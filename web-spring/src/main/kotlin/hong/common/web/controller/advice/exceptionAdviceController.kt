package hong.common.web.controller.advice

import hong.common.exception.BaseException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.full.findAnnotation

@ControllerAdvice
class ExceptionAdviceController {

    @ExceptionHandler(BaseException::class)
    fun baseException(request: HttpServletRequest, response: HttpServletResponse, e: BaseException): String {
        val responseStatus = e::class.findAnnotation<ResponseStatus>()
        val httpStatus = responseStatus?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
        val statusCode = httpStatus.value()
        response.status = statusCode
        return "error/$statusCode"
    }
}