package hong.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Configuration
class RetrofitConfig {

    @Bean
    fun retrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8080")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    }
}
