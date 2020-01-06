package hong.controller

import hong.common.auth.Authorize
import hong.common.auth.PolicyAuthentication
import hong.common.auth.PolicyAuthorize
import hong.common.auth.Principal
import hong.common.web.controller.BaseController
import hong.common.web.router.Router
import hong.integration.spring.retrofit.annotation.RetrofitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import retrofit2.Call
import retrofit2.http.GET

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Authorize
annotation class AdminAuthorize(
    val roles: Array<String> = ["admin"]
)

@Controller
@RequestMapping("/login-admin")
@AdminAuthorize
class LoginAdminController : BaseController() {

    @GetMapping("/test")
    fun test() {
        println()
    }

    @GetMapping("/test2")
    fun test2() {
        println()
    }

}


@Component
class SimplePolicyAuthentication : PolicyAuthentication {

    override fun handle(principal: Principal): Boolean {
        return true
    }

}

@PolicyAuthorize(policy = SimplePolicyAuthentication::class)
abstract class PolicyController1

@PolicyAuthorize(policy = SimplePolicyAuthentication::class)
abstract class PolicyController2 : PolicyController1()

@Controller
@RequestMapping("/login-multi-policy")
@PolicyAuthorize(policy = SimplePolicyAuthentication::class)
class LoginMultiController : PolicyController2() {

    @GetMapping("/test")
    @Authorize(roles = ["admin, user"])
    fun test() {
        println()
    }

    @GetMapping("/test2")
    fun test2() {
        println()
    }

}

@Controller
@RequestMapping("/login-policy")
@PolicyAuthorize(policy = SimplePolicyAuthentication::class)
class LoginPolicyController : BaseController() {

    @GetMapping("/test")
    @Authorize(roles = ["admin, user"])
    fun test() {
        println()
    }

    @GetMapping("/test2")
    fun test2() {
        println()
    }

}

@Controller
@RequestMapping("/login-role")
class LoginRoleController : BaseController() {

    @GetMapping("/test")
    @Authorize(roles = ["admin, user"])

    fun test() {
        println()
    }

    @GetMapping("/test2")
    fun test2() {
        println()
    }

}

@Controller
@RequestMapping("/login-class")
@Authorize
class LoginClassController : BaseController() {

    @GetMapping("/test")
    fun test() {
        println()
    }

}

@Controller
@RequestMapping("/login-method")
class LoginMethodController : BaseController() {

    @GetMapping("/test")
    @Authorize
    fun test() {
        println()
    }

}

@Controller
@RequestMapping("/test-arg/{hong1}")
class ArgController {

    @GetMapping("/test/{userId}/test222")
    fun pathVarialbeTest(@PathVariable("userId") userId: String) {

    }

    @GetMapping("/test/test222")
    fun pathVarialbeTest22222222() {

    }

}

@Controller
class MainController : BaseController() {

    @Autowired
    lateinit var githubApiService: GithubApiService

    @Autowired
    lateinit var router: Router

    @GetMapping(value = ["", "/", "/index"])
    fun main(model: Model): String {
        val body = githubApiService.get().execute().body()

        model.addAttribute("name", body)
        return "main"
    }

    @GetMapping("/router")
    fun router() {
        val mvcUrl2 = router.mvcUrl(ArgController::pathVarialbeTest, "123")
            .buildAndExpand(mapOf("hong1" to "55555555")).toUriString()

        val mvcUrl = router.mvcUrl(ArgController::pathVarialbeTest22222222)
            .buildAndExpand(mapOf("hong1" to "55555555")).toUriString()

        println()
    }

    @GetMapping("/badRequest")
    fun badRequest11() {
        badRequest()
    }

    @GetMapping("/unauthorized")
    fun unauthorized11() {
        unauthorized()
    }

    @GetMapping("/notFound")
    fun notFound111() {
        notFound()
    }
}

@RetrofitService
interface GithubApiService {

    @GET("/get")
    fun get(): Call<String>

}