package com.neo.caption.ocr.common

import com.github.benmanes.caffeine.cache.Caffeine
import com.neo.caption.ocr.handler.AsyncExceptionHandler
import com.neo.caption.ocr.handler.CaffeineCacheErrorHandler
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Duration

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
        .expireAfterAccess(Duration.ofHours(Int.MAX_VALUE.toLong()))
        .initialCapacity(2 shl 6)
        .maximumSize((2 shl 8).toLong())
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