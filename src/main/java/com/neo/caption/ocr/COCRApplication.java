package com.neo.caption.ocr;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan("com.neo.caption.ocr.property")
public class COCRApplication {

    @PostConstruct
    public void init() {
        Loader.load(opencv_java.class);
    }

    public static void main(String... args) {
        //noinspection resource
        SpringApplication.run(COCRApplication.class, args);
    }

}
