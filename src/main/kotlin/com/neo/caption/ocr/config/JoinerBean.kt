package com.neo.caption.ocr.config

import com.google.common.base.Joiner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JoinerBean {

    @Bean
    fun dotJoiner() = Joiner.on(".")

    @Bean
    fun plusJoiner() = Joiner.on("+")

    @Bean
    fun arrowJoiner() = Joiner.on(" -> ")

}
