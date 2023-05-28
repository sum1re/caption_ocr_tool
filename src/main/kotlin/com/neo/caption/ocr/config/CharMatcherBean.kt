package com.neo.caption.ocr.config

import com.google.common.base.CharMatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CharMatcherBean {

    @Bean
    fun dotCharMatcher() = CharMatcher.`is`('.')

}
