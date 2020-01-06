package hong.common.form

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.datetime.standard.DateTimeFormatterFactory
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.time.temporal.TemporalAccessor
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
            .map { Triple(it.name, it.getter.call(form), it.annotations) }

        return fields
            .fold(UriComponentsBuilder.newInstance(), { builder, (key, value, annotations) ->
                builder.queryParam(key, buildParam(value, annotations))
            })
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString()
            .removePrefix("?")
    }

    private fun buildParam(value: Any?, annotations: List<Annotation>): List<String> {
        if (value == null) {
            return emptyList()
        }
        val list = when (value) {
            is Array<*> -> value.map { it }
            is Collection<*> -> value
            else -> listOf(value)
        }
        return list.mapNotNull { formatter(it, annotations) }
    }

    private fun formatter(value: Any?, annotations: List<Annotation>): String? {
        if (value == null) return null
        if (value is Enum<*>) return value.name
        if (value is TemporalAccessor) {
            val dateTimeFormat = annotations.firstOrNull { it is DateTimeFormat } as? DateTimeFormat
            if (dateTimeFormat != null) {
                val dateTimeFormatter = DateTimeFormatterFactory().run {
                    if (dateTimeFormat.style.isNotBlank()) {
                        setStylePattern(dateTimeFormat.style)
                    }
                    if (dateTimeFormat.pattern.isNotBlank()) {
                        setPattern(dateTimeFormat.pattern)
                    }
                    createDateTimeFormatter()
                }
                return dateTimeFormatter.format(value)
            }
        }
        // TODO : support Date, Calendar
        return value.toString().trim().ifEmpty { null }
    }
}

open class PageForm(@Parameter open val page: Int = 1, @Parameter val limit: Int = 10) : Form