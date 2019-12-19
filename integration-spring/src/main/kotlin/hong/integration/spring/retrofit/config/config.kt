package hong.integration.spring.retrofit.config

import hong.integration.spring.retrofit.annotation.RetrofitService
import hong.integration.spring.retrofit.autoconfigure.RetrofitServiceScan
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils
import retrofit2.Retrofit
import kotlin.reflect.full.findAnnotation

class RetrofitServiceRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val packages = getPackagesToScan(importingClassMetadata)
        val provider = RetrofitServiceComponentProvider()
        packages
            .flatMap { provider.findCandidateComponents(it) }
            .forEach { register(it, registry) }
    }

    private fun getPackagesToScan(importingClassMetadata: AnnotationMetadata): Set<String> {
        val attributes =
            AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RetrofitServiceScan::class.java.name))

        val value = attributes!!.getStringArray(RetrofitServiceScan::value.name)

        val packagesToScan = mutableSetOf<String>()
        packagesToScan.addAll(value)

        if (packagesToScan.isEmpty()) {
            return setOf(ClassUtils.getPackageName(importingClassMetadata.className))
        }

        return packagesToScan
    }

    private fun register(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry) {
        val builder = BeanDefinitionBuilder.rootBeanDefinition(RetrofitServiceFactoryBean::class.java)
        builder.addConstructorArgValue(beanDefinition.beanClassName)
        registry.registerBeanDefinition(beanDefinition.beanClassName!!, builder.beanDefinition)
    }
}

class RetrofitServiceFactoryBean(private val beanClass: Class<Any>) : AbstractFactoryBean<Any>() {

    @Autowired
    lateinit var applicationContext: AbstractApplicationContext

    override fun createInstance(): Any {
        val retrofitService = beanClass.kotlin.findAnnotation<RetrofitService>()!!
        val retrofit = applicationContext.getBean(retrofitService.value, Retrofit::class.java)
        return retrofit.create(beanClass)
    }

    override fun getObjectType(): Class<*>? = Any::class.javaObjectType
}

class RetrofitServiceComponentProvider : ClassPathScanningCandidateComponentProvider(false) {

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean =
        beanDefinition.metadata.isInterface

    init {
        addIncludeFilter(AnnotationTypeFilter(RetrofitService::class.java, true, true))
    }
}