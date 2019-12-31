package hong.common.config

import hong.common.auth.AuthorizeHandlerInterceptorAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    @Autowired
    lateinit var authorizeHandlerInterceptorAdapter: AuthorizeHandlerInterceptorAdapter

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        registry.addInterceptor(authorizeHandlerInterceptorAdapter)
    }
}