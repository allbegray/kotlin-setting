package hong.controller

import hong.common.auth.Authorize
import hong.common.auth.PolicyAuthentication
import hong.common.auth.PolicyAuthorize
import hong.common.auth.Principal
import hong.common.web.controller.BaseController
import hong.common.web.router.ReverseRouter
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
@Authorize(roles = ["admin"])
annotation class AdminAuthorize

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
class SimplePolicyAuthentication1 : PolicyAuthentication {

    override fun handle(principal: Principal): Boolean {
        return true
    }

}

@Component
class SimplePolicyAuthentication2 : PolicyAuthentication {

    override fun handle(principal: Principal): Boolean {
        return true
    }

}

@Component
class SimplePolicyAuthentication3 : PolicyAuthentication {

    override fun handle(principal: Principal): Boolean {
        return true
    }

}

@PolicyAuthorize(policy = SimplePolicyAuthentication1::class)
abstract class PolicyController1

@PolicyAuthorize(policy = SimplePolicyAuthentication2::class)
abstract class PolicyController2 : PolicyController1()

@Controller
@RequestMapping("/login-multi-policy")
@PolicyAuthorize(policy = SimplePolicyAuthentication3::class)
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
@PolicyAuthorize(policy = SimplePolicyAuthentication1::class)
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

    @Autowired
    lateinit var reverseRouter: ReverseRouter

    @GetMapping("/test/{userId}/test222")
    fun pathVarialbeTest(@PathVariable("userId") userId: String) {
        val currentUrlFor = reverseRouter.currentUrlFor()
        println()
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
    lateinit var reverseRouter: ReverseRouter

    @GetMapping(value = ["", "/", "/index"])
    fun main(model: Model): String {
        val body = githubApiService.get().execute().body()

        model.addAttribute("name", body)

        g.alert("test\ntest")

        return "main"
    }

    @GetMapping("/router")
    fun router() {
        val mvcUrl2 = reverseRouter.urlFor(ArgController::pathVarialbeTest).args("hong1" to "555", "userId" to "aaaaaaa").build()

        val mvcUrl = reverseRouter.urlFor(ArgController::pathVarialbeTest22222222).args("hong1" to "555").build()

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