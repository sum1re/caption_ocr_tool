package com.neo.caption.ocr.common

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
@EnableWebMvc
class MvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("/resources/", "file:${System.getProperty("java.io.tmpdir")}")
    }
}

@Configuration
@Import(ExposedAutoConfiguration::class)
class ExposedConfig
