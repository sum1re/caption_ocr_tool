package com.neo.caption.ocr.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager();
        var caffeine = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(Integer.MAX_VALUE))
                .initialCapacity(2 << 6)
                .maximumSize(2 << 8);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
