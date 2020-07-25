package com.neo.caption.ocr.config;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.neo.caption.ocr.CaptionOCR;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.videoio.VideoCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

@Configuration
@Slf4j
public class AppConfig {

    /**
     * i18n
     */
    @Bean
    public ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("language");
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public Preferences preferences() {
        return Preferences.userRoot().node(CaptionOCR.class.getPackageName());
    }

    @Bean
    public TessBaseAPI tessBaseAPI() {
        return new TessBaseAPI();
    }

    @Bean(name = "dot")
    public Joiner dotJoiner() {
        return Joiner.on(".");
    }

    @Bean(name = "plus")
    public Joiner plusJoiner() {
        return Joiner.on("+");
    }

    @Bean(name = "arrow")
    public Joiner arrowJoiner() {
        return Joiner.on(" -> ");
    }

    @Bean(name = "comma")
    public Splitter commaSplitter() {
        return Splitter.on(",");
    }

    @Bean(name = "plusSplitter")
    public Splitter plusSplitter() {
        return Splitter.on("+");
    }

    // Editor splitter
    @Bean(name = "lineSeparator")
    public Splitter lineSeparatorSplitter() {
        return Splitter.on("\n");
    }

    @Bean
    public CharMatcher dotCharMatcher() {
        return CharMatcher.is('.');
    }

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
