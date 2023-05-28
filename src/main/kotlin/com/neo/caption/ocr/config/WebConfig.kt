package com.neo.caption.ocr.config

import com.neo.caption.ocr.property.CorsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class WebConfig(
    val corsProperties: CorsProperties
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