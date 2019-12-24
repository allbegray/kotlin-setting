package hong.common.web.ajax

import org.springframework.context.MessageSource
import org.springframework.validation.BindingResult
import javax.servlet.http.HttpServletRequest

object Ajax {

    fun isAjax(request: HttpServletRequest): Boolean {
        val requestedWithHeader: String = request.getHeader("X-Requested-With")
        return "XMLHttpRequest" == requestedWithHeader
    }

    data class Response<T>(
        val result: Result,
        val message: String? = null,
        val data: T? = null,
        val errors: Map<String, List<String>> = emptyMap()
    ) {
        companion object {
            fun <T> ofSuccess(data: T): Response<T> = Response(Result.SUCCESS, data = data)

            fun ofFailure(message: String): Response<Unit> {
                return Response(Result.FAILURE, message = message)
            }

            fun ofFailure(bindResult: BindingResult, messageSource: MessageSource): Response<Unit> {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                val errors = bindResult.fieldErrors
                    .map {
                        it.field to messageSource.getMessage(it, null)
                    }
                    .groupBy({ it.first }, { it.second })
                return Response(Result.FAILURE, errors = errors)
            }

            fun ofFailure(ex: Exception): Response<Unit> {
                return Response(Result.FAILURE, message = if (ex.cause != null) ex.cause!!.message else ex.message)
            }
        }

        enum class Result {
            SUCCESS, FAILURE
        }
    }
}



