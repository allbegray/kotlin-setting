package hong.integration.spring.retrofit.config

import hong.integration.spring.retrofit.annotation.RetrofitService
import hong.integration.spring.retrofit.autoconfigure.EnableRetrofitAutoConfiguration
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
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

class RetrofitRegistrar : ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private var beanFactory: BeanFactory? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val packages = getPackagesToScan(importingClassMetadata)
        val provider = RetrofitServiceComponentProvider()
        packages
            .flatMap { provider.findCandidateComponents(it) }
            .forEach { register(it, registry) }
    }

    private fun register(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry) {
        val builder = BeanDefinitionBuilder.rootBeanDefinition(RetrofitFactoryBean::class.java)
        builder.addConstructorArgValue(beanDefinition.beanClassName)
        registry.registerBeanDefinition(beanDefinition.beanClassName!!, builder.beanDefinition)
    }

    private fun getPackagesToScan(importingClassMetadata: AnnotationMetadata): Set<String> {
        val attributes =
            AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRetrofitAutoConfiguration::class.java.name))

        val value = attributes!!.getStringArray(EnableRetrofitAutoConfiguration::value.name)

        val packagesToScan = mutableSetOf<String>()
        packagesToScan.addAll(value)

        if (packagesToScan.isEmpty()) {
            return setOf(ClassUtils.getPackageName(importingClassMetadata.className))
        }

        return packagesToScan
    }
}

class RetrofitFactoryBean(private val beanClass: Class<Any>) : AbstractFactoryBean<Any>() {

    @Autowired
    lateinit var applicationContext: AbstractApplicationContext

    override fun createInstance(): Any {
        val retrofitService = beanClass.kotlin.findAnnotation<RetrofitService>()!!
        val retrofit = applicationContext.getBean(retrofitService.value, Retrofit::class.java)
        return retrofit.create(beanClass)
    }

    override fun getObjectType(): Class<*>? {
        return Any::class.javaObjectType
    }
}

class RetrofitServiceComponentProvider : ClassPathScanningCandidateComponentProvider(false) {

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean =
        beanDefinition.metadata.isInterface

    init {
        addIncludeFilter(AnnotationTypeFilter(RetrofitService::class.java, true, true))
    }
}


