package hong

import hong.integration.spring.retrofit.autoconfigure.RetrofitServiceScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@RetrofitServiceScan(["hong"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}