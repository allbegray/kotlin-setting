package hong.common.web.router

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

abstract class AbstractUriBuilder {

    private data class Param(val replace: Boolean, val name: String, val values: List<Any?>)

    abstract val uriComponentsBuilder: UriComponentsBuilder
    private val queryParams: MutableList<Param> = mutableListOf()

    fun queryParam(name: String, vararg values: Any?): AbstractUriBuilder {
        this.queryParams.add(Param(false, name, values.toList()))
        return this
    }

    fun replaceQueryParam(name: String, vararg values: Any?): AbstractUriBuilder {
        this.queryParams.add(Param(true, name, values.toList()))
        return this
    }

    protected fun bindParams(cloneBuilder: UriComponentsBuilder): UriComponentsBuilder {
        return queryParams
            .fold(cloneBuilder, { builder, (replace, name, values) ->
                if (replace) {
                    builder.replaceQueryParam(name, values)
                } else {
                    builder.queryParam(name, values)
                }
            })
    }

    abstract fun build(): String

    override fun toString(): String = build()
}

class UriBuilder(path: String) : AbstractUriBuilder() {
    override val uriComponentsBuilder: UriComponentsBuilder = UriComponentsBuilder.fromPath(path)
    private val uriVariables = mutableMapOf<String, Any?>()

    fun arg(key: String, value: Any?): UriBuilder {
        uriVariables[key] = value
        return this
    }

    fun args(args: Map<String, Any?>): UriBuilder {
        uriVariables.putAll(args)
        return this
    }

    fun args(vararg args: Pair<String, Any?>): UriBuilder {
        uriVariables.putAll(args.toMap())
        return this
    }

    override fun build(): String {
        return bindParams(uriComponentsBuilder.cloneBuilder())
            .buildAndExpand(uriVariables)
            .encode(StandardCharsets.UTF_8)
            .toUriString()
    }
}

class CurrentUriBuilder(private val request: HttpServletRequest) : AbstractUriBuilder() {
    override val uriComponentsBuilder: UriComponentsBuilder = UriComponentsBuilder.fromUriString(request.requestURI)
    private var includeQueryString: Boolean = false

    fun withQueryString(): CurrentUriBuilder {
        this.includeQueryString = true
        return this
    }

    override fun build(): String {
        val cloneBuilder = uriComponentsBuilder.cloneBuilder().let {
            if (includeQueryString) {
                it.query(request.queryString)
            } else {
                it
            }
        }
        return bindParams(cloneBuilder)
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString()
    }
}

@Component
class ReverseRouter {

    @Autowired
    lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    @Autowired
    lateinit var request: HttpServletRequest

    companion object {
        private const val SEPARATOR = "."
    }

    fun currentUrlFor(): CurrentUriBuilder {
        return CurrentUriBuilder(request)
    }

    fun urlFor(function: KFunction<*>): UriBuilder {
        val methodName = function.javaMethod!!
        val controllerName = methodName.declaringClass.simpleName
        return urlFor(controllerName + SEPARATOR + methodName.name)
    }

    /**
     * @param pattern controllerName#methodName (example : mainController#main)
     */
    fun urlFor(pattern: String): UriBuilder {
        val split = pattern.split(SEPARATOR)
        if (split.size != 2) {
            throw RuntimeException("pattern error")
        }

        val (controllerName, methodName) = split[0].decapitalize() to split[1]

        val handler = requestMappingHandlerMapping.handlerMethods
            .filter { (_, handler) ->
                controllerName == handler.beanType.simpleName.decapitalize() && methodName == handler.method.name
            }
            .toList()
            .firstOrNull()

        @Suppress("FoldInitializerAndIfToElvis")
        if (handler == null) {
            throw RuntimeException("not found handler")
        }

        return UriBuilder(handler.first.patternsCondition.patterns.toList()[0])
    }

}