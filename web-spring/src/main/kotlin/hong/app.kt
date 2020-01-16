package hong

import hong.common.storage.StorageProperties
import hong.integration.spring.retrofit.autoconfigure.RetrofitServiceScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [StorageProperties::class])
@RetrofitServiceScan(["hong"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}