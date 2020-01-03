package hong.common.form

import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Parameter

interface Form {

    fun query(): String {
        val form = this

        val fields = (listOf(this::class) + this::class.allSuperclasses)
            .flatMap { it.declaredMemberProperties }
            .filter { it.javaField?.getAnnotation(Parameter::class.java) != null }
            .map { it.name to it.getter.call(form) }

        return fields
            .fold(UriComponentsBuilder.newInstance(), { builder, (key, value) ->
                builder.queryParam(key, value)
            })
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString()
            .removePrefix("?")
    }
}

open class PageForm(@Parameter open val page: Int = 1, @Parameter val limit: Int = 10) : Form

class TestSearchForm(@Parameter val foo: String, @Parameter val bar: String, @Parameter val checkbox : List<String>, override val page: Int) : PageForm(page)

fun main() {
    val pageForm = TestSearchForm("1", "한글", listOf("1", "2", "3"), 1)
    val x = pageForm.query()
    println(x)

    val toUriString = UriComponentsBuilder.newInstance().queryParam("key", 1, 2, 3).build().toUriString()
    println(toUriString)
}