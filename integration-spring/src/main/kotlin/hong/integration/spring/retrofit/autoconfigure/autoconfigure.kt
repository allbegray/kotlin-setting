package hong.integration.spring.retrofit.autoconfigure

import hong.integration.spring.retrofit.config.RetrofitServiceRegistrar
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Import(RetrofitServiceRegistrar::class)
annotation class RetrofitServiceScan(val value: Array<String>)