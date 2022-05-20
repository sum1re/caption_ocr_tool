package com.neo.caption.ocr.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.neo.caption.ocr.COCRApplication;
import com.neo.caption.ocr.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogServiceImpl implements LogService {

    @Override
    public void modifyLogLevel(Level level) {
        log.warn("modify log level to: {}", level.levelStr);
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(COCRApplication.class.getPackageName()).setLevel(level);
    }

}
