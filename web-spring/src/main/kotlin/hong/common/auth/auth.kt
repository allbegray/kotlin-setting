package hong.common.auth

import hong.common.storage.SessionStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.lang.annotation.Inherited
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass

interface Principal {
    val username: String
    val role: String
}

@Component
class AuthService {

    companion object {
        const val PRINCIPAL_SESSION_KEY = "PRINCIPAL_SESSION_KEY"
    }

    @Autowired
    lateinit var sessionStorageService: SessionStorageService

    fun principal(): Principal? = sessionStorageService.get<Principal>(PRINCIPAL_SESSION_KEY)

    fun isLogin(): Boolean = principal() != null

    fun set(principal: Principal) {
        sessionStorageService.set(PRINCIPAL_SESSION_KEY, principal)
    }

    fun invalidate() {
        sessionStorageService.remove(PRINCIPAL_SESSION_KEY)
        sessionStorageService.expire()
    }
}

/**
 * or condition
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Repeatable
annotation class Authorize(val roles: Array<String> = [])

/**
 * and condition
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Repeatable
annotation class PolicyAuthorize(val policy: KClass<out PolicyAuthentication>)

/**
 * top level
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
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

    @Autowired
    lateinit var authService: AuthService

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val annotations = listOf(handler.method, handler.beanType)
                .flatMap { AnnotatedElementUtils.findAllMergedAnnotations(it, annotationTypes) }
                .toSet()

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
                val validPolicy = validPolicy(principal, annotations.filterIsInstance<PolicyAuthorize>())

                if (validRole && validPolicy) {
                    return true
                }
            }

            // TODO : ajax or not
            response.sendRedirect("/login")
            return false
        }

        return super.preHandle(request, response, handler)
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

    private fun principal(): Principal? = authService.principal()

}