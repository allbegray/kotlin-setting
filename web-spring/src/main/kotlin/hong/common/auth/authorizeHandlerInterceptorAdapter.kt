package hong.common.auth

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Authorize(val roles: Array<String> = [])

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AllowAnonymous

@Component
class AuthorizeHandlerInterceptorAdapter : HandlerInterceptorAdapter() {

    private val annotationTypes = setOf(Authorize::class.java, AllowAnonymous::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val annotations = listOf(handler.method, handler.beanType)
                .flatMap { AnnotatedElementUtils.findAllMergedAnnotations(it, annotationTypes) }

            if (annotations.isEmpty()) {
                return true
            }

            // AllowAnonymous is top level
            if (annotations.filterIsInstance<AllowAnonymous>().any()) {
                return true
            }

            val user = user()
            if (user == null) {
                return false
            }

            val authorizes = annotations.filterIsInstance<Authorize>()
            val roles = authorizes
                .flatMap { it.roles.toList() }
                .flatMap { it.split(",") }
                .map(String::trim)
                .filterNot { it.isBlank() }

            if (roles.contains(user.role)) {
                return true
            }

            return false
        }

        return true
    }

    private fun user(): User? {
        TODO("get session user")
    }

}