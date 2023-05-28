package com.neo.caption.ocr.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.neo.caption.ocr.handler.CaffeineCacheErrorHandler
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

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