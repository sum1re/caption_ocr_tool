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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
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
class WebConfig(
    private val corsProperties: CorsProperties
) {

    @Bean
    fun corsConfigurationSource() = CorsConfiguration()
        .let {
            it.allowCredentials = true
            it.allowedOriginPatterns = corsProperties.originPatterns
            it.allowedMethods = corsProperties.allowedMethods
            it.allowedHeaders = corsProperties.allowedHeader
            UrlBasedCorsConfigurationSource().apply { this.registerCorsConfiguration("/**", it) }
        }

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
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("/resources/", "file:${commonProperties.workingDirectory}/")
    }
}

@Configuration
@Import(ExposedAutoConfiguration::class)
class ExposedConfig
