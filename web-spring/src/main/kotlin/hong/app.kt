package hong

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
class DemoApplication

@Controller
class MainController {

    @GetMapping(value = ["", "/", "/index"])
    fun main(model: Model): String {
        model.addAttribute("name", "홍길동")
        return "main"
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}