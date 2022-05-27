package com.neo.caption.ocr;

import com.neo.caption.ocr.service.LoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan("com.neo.caption.ocr.property")
@RequiredArgsConstructor
public class COCRApplication {

    private final LoaderService loaderService;

    @PostConstruct
    public void init() {
        loaderService.logInfo();
        loaderService.loadLib(opencv_java.class);
    }

    public static void main(String... args) {
        //noinspection resource
        SpringApplication.run(COCRApplication.class, args);
    }

}
