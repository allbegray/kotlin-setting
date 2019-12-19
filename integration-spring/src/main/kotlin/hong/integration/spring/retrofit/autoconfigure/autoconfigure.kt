package hong.integration.spring.retrofit.autoconfigure

import hong.integration.spring.retrofit.config.RetrofitServiceRegistrar
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(RetrofitServiceRegistrar::class)
annotation class RetrofitServiceScan(val value: Array<String>)