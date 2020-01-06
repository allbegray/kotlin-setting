package hong.common.web.router

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * buildAndExpand 는 method 에 있는 @RequestMapping 상단에 있는 @RequestMapping path variable 을
 * args 는 method 에 있는 path variable 을 의미하기 때문에
 * TODO : MvcUriComponentsBuilder 나  MvcUriComponentsBuilder.MethodArgumentBuilder 를 좀 더 깔끔하게 맵핑 시켜 줄 아이가 필요 하다.
 */

@Component
class Router {

    companion object {
        private const val SEPARATOR = RequestMappingInfoHandlerMethodMappingNamingStrategy.SEPARATOR
    }

    fun mvcUrl(function: KFunction<*>, vararg args: Any = emptyArray()): UriComponentsBuilder {
        val method = function.javaMethod!!
        val controllerType = method.declaringClass
        return MvcUriComponentsBuilder.fromMethod(controllerType, method, *args)
    }

    fun mvcUrl(pattern: String): MvcUriComponentsBuilder.MethodArgumentBuilder {
        val mappingName = if (pattern.contains("#")) {
            val controller = pattern.substringBeforeLast(SEPARATOR).substringAfterLast(".")
            val method = pattern.substringAfterLast(SEPARATOR)
            controller.filter { it.isUpperCase() } + SEPARATOR + method
        } else {
            pattern
        }
        return MvcUriComponentsBuilder.fromMappingName(mappingName)
    }

}