package com.neo.caption.ocr.common

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.springframework.core.convert.converter.ConverterRegistry
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    override fun getAsyncUncaughtExceptionHandler() = AsyncExceptionHandler()
}

@Configuration
@EnableCaching
class CacheConfig : CachingConfigurer {

    @Bean
    override fun cacheManager() = Caffeine.newBuilder()
        .initialCapacity(2 shl 6)
        .let { CaffeineCacheManager().apply { this.setCaffeine(it) } }

    @Bean
    override fun errorHandler() = CaffeineCacheErrorHandler()

}

@Configuration
class ConverterConfiguration(
    private val autoRegisteredConverters: Set<Converter<*, *>>,
    private val autoRegisteredConverterFactories: Set<ConverterFactory<*, *>>,
    private val converterRegistry: ConverterRegistry,
) {
    @PostConstruct
    fun conversionService() {
        autoRegisteredConverters.forEach { converterRegistry.addConverter(it) }
        autoRegisteredConverterFactories.forEach { converterRegistry.addConverterFactory(it) }
    }
}

@Configuration
@EnableWebMvc
class MvcConfig(
    private val commonProperties: CommonProperties,
    private val corsProperties: CorsProperties
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedOriginPatterns(*corsProperties.originPatterns.toTypedArray())
            .allowedMethods(*corsProperties.allowedMethods.toTypedArray())
            .allowedHeaders(*corsProperties.allowedHeader.toTypedArray())
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**", "/media/**")
            .addResourceLocations("/resources/", "file:${commonProperties.workingDirectory}/")
    }
}

@Configuration
@Import(ExposedAutoConfiguration::class)
class ExposedConfig
