package hong.integration.spring.retrofit.autoconfigure

import hong.integration.spring.retrofit.config.RetrofitRegistrar
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.annotation.Inherited

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Import(value = [RetrofitAutoConfiguration::class, RetrofitRegistrar::class])
annotation class EnableRetrofitAutoConfiguration(val value: Array<String>)

@ConditionalOnClass(Retrofit::class)
class RetrofitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = [Retrofit::class])
    fun retrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8080")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    }
}