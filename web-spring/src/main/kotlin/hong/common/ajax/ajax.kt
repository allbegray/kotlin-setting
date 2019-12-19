package hong.common.ajax

import org.springframework.context.MessageSource
import org.springframework.validation.BindingResult

enum class Result {
    SUCCESS, FAILURE
}

data class AjaxResponse<T>(
    val result: Result,
    val message: String? = null,
    val data: T? = null,
    val errors: Map<String, List<String>> = emptyMap()
) {
    companion object {
        fun <T> ofSuccess(data: T): AjaxResponse<T> = AjaxResponse(Result.SUCCESS, data = data)

        fun ofFailure(message: String): AjaxResponse<Unit> {
            return AjaxResponse(Result.FAILURE, message = message)
        }

        fun ofFailure(bindResult: BindingResult, messageSource: MessageSource): AjaxResponse<Unit> {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            val errors = bindResult.fieldErrors
                .map {
                    it.field to messageSource.getMessage(it, null)
                }
                .groupBy({ it.first }, { it.second })
            return AjaxResponse(Result.FAILURE, errors = errors)
        }

        fun ofFailure(ex: Exception): AjaxResponse<Unit> {
            return AjaxResponse(Result.FAILURE, message = if (ex.cause != null) ex.cause!!.message else ex.message)
        }

    }
}