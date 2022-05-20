package com.neo.caption.ocr.config;

import com.google.common.base.Splitter;
import com.neo.caption.ocr.property.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;
    @Qualifier("commaSplitter")
    private final Splitter splitter;

    @Bean
    public CorsFilter corsFilter() {
        var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOriginPatterns(corsConfiguration.getAllowedOriginPatterns());
        corsConfiguration.addAllowedHeader(corsProperties.getAllowedHeader());
        corsConfiguration.setAllowedMethods(splitter.splitToList(corsProperties.getAllowedMethods()));
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

}
