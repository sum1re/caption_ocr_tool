package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.property.AppInfoProperties;
import com.neo.caption.ocr.service.LoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoaderServiceImpl implements LoaderService {

    private final AppInfoProperties appInfoProperties;

    @Override
    @Async
    public void loadLib(Class<?>... classes) {
        //noinspection ConfusingArgumentToVarargsMethod
        log.info("loading: {}", classes);
        Loader.load(classes, false);
    }

    @Override
    @Async
    public void logInfo() {
        log.info("system info: [platform={}, chips={}, core={}, processors={}]",
                Loader.getPlatform(), Loader.totalChips(), Loader.totalCores(), Loader.totalProcessors());
        try {
            log.info("javacpp info: [cache={}, temp={}, version={}]",
                    Loader.getCacheDir(), Loader.getTempDir(), Loader.getVersion());
        } catch (IOException e) {
            log.error("Failed to get javacpp information");
            e.printStackTrace();
        }
        log.info("app info: [JavaVersion={}, SpringBootVersion={}, appVersion={}, build={}]",
                appInfoProperties.getJavaVersion(), appInfoProperties.getSpringBootVersion(),
                appInfoProperties.getVersion(), appInfoProperties.getBuildTimestamp());
    }

}
