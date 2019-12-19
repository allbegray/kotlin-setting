package hong.integration.spring.retrofit.config

import hong.integration.spring.retrofit.annotation.RetrofitService
import hong.integration.spring.retrofit.autoconfigure.RetrofitServiceScan
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils
import retrofit2.Retrofit

class RetrofitServiceRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        if (!registry.containsBeanDefinition(RetrofitServiceBeanPostProcessorAdapter.BEAN_NAME)) {
            registry.registerBeanDefinition(
                RetrofitServiceBeanPostProcessorAdapter.BEAN_NAME,
                RootBeanDefinition(RetrofitServiceBeanPostProcessorAdapter::class.java)
            )
        }

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
        val beanName = generateBeanName(beanDefinition)
        registry.registerBeanDefinition(beanName, beanDefinition)
    }

    private fun generateBeanName(beanDefinition: BeanDefinition): String {
        try {
            val beanClass = Class.forName(beanDefinition.beanClassName)

            val retrofitService = beanClass.getAnnotation(RetrofitService::class.java)
            if (retrofitService != null && retrofitService.name.isNotBlank()) {
                return retrofitService.name
            }

            val qualifier = beanClass.getAnnotation(Qualifier::class.java)
            if (qualifier != null && qualifier.value.isNotBlank()) {
                return qualifier.value
            }

            return beanClass.name
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Cannot obtain bean name for Retrofit service interface", e)
        }
    }
}

class RetrofitServiceComponentProvider : ClassPathScanningCandidateComponentProvider(false) {

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean =
        beanDefinition.metadata.isInterface

    init {
        addIncludeFilter(AnnotationTypeFilter(RetrofitService::class.java, true, true))
    }
}

@Suppress("INTEGER_OVERFLOW")
class RetrofitServiceBeanPostProcessorAdapter : InstantiationAwareBeanPostProcessorAdapter(), BeanFactoryAware {

    companion object {
        const val BEAN_NAME = "retrofitServiceBeanPostProcessorAdapter"
    }

    private lateinit var beanFactory: BeanFactory

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun postProcessBeforeInstantiation(beanClass: Class<*>, beanName: String): Any? {
        if (beanClass.isAnnotationPresent(RetrofitService::class.java)) {
            val retrofitService = beanClass.getAnnotation(RetrofitService::class.java)
            val retrofit = beanFactory.getBean(retrofitService.value, Retrofit::class.java)
            return retrofit.create(beanClass)
        }
        return super.postProcessBeforeInstantiation(beanClass, beanName)
    }
}