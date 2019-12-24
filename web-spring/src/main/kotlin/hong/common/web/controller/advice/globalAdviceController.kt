package hong.common.web.controller.advice

import hong.common.web.controller.RequestGlobal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalAdviceController {

    @Autowired
    lateinit var g: RequestGlobal

    @ModelAttribute("g")
    fun requestGlobal(): RequestGlobal = g

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }

}