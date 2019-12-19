package hong

import hong.integration.spring.retrofit.annotation.RetrofitService
import hong.integration.spring.retrofit.autoconfigure.RetrofitServiceScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
@RetrofitServiceScan(["hong"])
class DemoApplication

@Controller
class MainController {

    @GetMapping(value = ["", "/", "/index"])
    fun main(model: Model, bindingResult: BindingResult): String {
        model.addAttribute("name", "홍길동")
        return "main"
    }
}

@RetrofitService
interface GithubApiService {
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}