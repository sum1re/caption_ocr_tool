package com.neo.caption.ocr.config

import com.google.common.base.Splitter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SplitterBean {

    @Bean
    fun commaSplitter() = Splitter.on(",")

    @Bean
    fun plusSplitter() = Splitter.on("+")

    @Bean
    fun lineSeparatorSplitter() = Splitter.on(System.lineSeparator())

}
