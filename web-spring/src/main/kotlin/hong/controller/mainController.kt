package hong.controller

import hong.integration.spring.retrofit.annotation.RetrofitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import retrofit2.Call
import retrofit2.http.GET

@Controller
class MainController {

    @Autowired
    lateinit var githubApiService: GithubApiService

    @GetMapping(value = ["", "/", "/index"])
    fun main(model: Model): String {
        val body = githubApiService.get().execute().body()

        model.addAttribute("name", body)
        return "main"
    }
}

@RetrofitService
interface GithubApiService {

    @GET("/get")
    fun get(): Call<String>

}