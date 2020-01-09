package hong.common.config

import hong.common.auth.AuthorizationHandlerInterceptorAdapter
import hong.common.interceptor.DebugInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    @Autowired
    lateinit var authorizationHandlerInterceptorAdapter: AuthorizationHandlerInterceptorAdapter

    @Autowired
    lateinit var debugInterceptor: DebugInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(debugInterceptor)
        super.addInterceptors(registry)
        registry.addInterceptor(authorizationHandlerInterceptorAdapter)
    }
}