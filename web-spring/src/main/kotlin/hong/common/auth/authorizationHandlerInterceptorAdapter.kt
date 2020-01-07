package hong.common.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * or condition
 */
annotation class Authorize(val roles: Array<String> = [])

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS/*, AnnotationTarget.FUNCTION*/)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * and condition
 */
annotation class PolicyAuthorize(val policy: KClass<out PolicyAuthentication>)

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * top level
 */
annotation class AllowAnonymous

interface PolicyAuthentication {
    fun handle(principal: Principal): Boolean
}

@Component
class AuthorizationHandlerInterceptorAdapter : HandlerInterceptorAdapter() {

    private val annotationTypes =
        setOf(Authorize::class.java, PolicyAuthorize::class.java, AllowAnonymous::class.java)

    @Autowired
    lateinit var applicationContext: ApplicationContext

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val targets = listOf(
                handler.method,
                handler.beanType
            ) + handler.beanType.kotlin.allSuperclasses.map { it.java }

            val annotations = targets.flatMap { AnnotatedElementUtils.findAllMergedAnnotations(it, annotationTypes) }

            if (annotations.isEmpty()) {
                return true
            }

            // AllowAnonymous is top level
            if (annotations.filterIsInstance<AllowAnonymous>().any()) {
                return true
            }

            val principal = principal()
            if (principal != null) {
                val validRole = validRole(principal, annotations.filterIsInstance<Authorize>())
                val validPolicy = validPolicy(principal, annotations.filterIsInstance<PolicyAuthorize>().toSet())

                if (validRole && validPolicy) {
                    return true
                }
            }

            // TODO : ajax or not
            response.sendRedirect("/login")
            return false
        }

        return true
    }

    private fun validRole(principal: Principal, authorizes: Collection<Authorize>): Boolean {
        if (authorizes.isEmpty()) {
            return true
        }

        val roles = authorizes
            .flatMap { it.roles.toList() }
            .flatMap { it.split(",") }
            .map(String::trim)
            .filterNot { it.isBlank() }

        return roles.contains(principal.role)
    }

    private fun validPolicy(principal: Principal, authorizes: Collection<PolicyAuthorize>): Boolean {
        if (authorizes.isEmpty()) {
            return true
        }

        val authentications = authorizes.map { applicationContext.getBean(it.policy.java) }

        return authorizes.size == authentications.filter { it.handle(principal) }.size
    }

    private fun principal(): Principal? {
        return object : Principal {
            override val username: String = "hong"
            override val role: String = "user"
        }
    }

}