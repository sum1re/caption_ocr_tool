package com.neo.caption.ocr.config;

import com.google.common.base.Joiner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JoinerBean {

    @Bean(name = "dotJoiner")
    public Joiner dotJoiner() {
        return Joiner.on(".");
    }

    @Bean(name = "plusJoiner")
    public Joiner plusJoiner() {
        return Joiner.on("+");
    }

    @Bean(name = "arrowJoiner")
    public Joiner arrowJoiner() {
        return Joiner.on(" -> ");
    }

}
