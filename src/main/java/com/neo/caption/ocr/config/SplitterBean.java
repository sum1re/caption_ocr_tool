package com.neo.caption.ocr.config;

import com.google.common.base.Splitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SplitterBean {

    @Bean(name = "commaSplitter")
    public Splitter commaSplitter() {
        return Splitter.on(",");
    }

    @Bean(name = "plusSplitter")
    public Splitter plusSplitter() {
        return Splitter.on("+");
    }

    // Editor splitter
    @Bean(name = "lineSeparatorSplitter")
    public Splitter lineSeparatorSplitter() {
        return Splitter.on("\n");
    }

}
