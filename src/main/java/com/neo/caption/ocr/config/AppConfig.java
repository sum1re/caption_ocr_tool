package com.neo.caption.ocr.config;

import org.opencv.videoio.VideoCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(5, 10, 0L,
                TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(1024));
    }

    @Bean
    public VideoCapture videoCapture() {
        return new VideoCapture();
    }
}
