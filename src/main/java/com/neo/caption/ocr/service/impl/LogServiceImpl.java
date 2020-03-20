package com.neo.caption.ocr.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.neo.caption.ocr.CaptionOCR;
import com.neo.caption.ocr.service.LogService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Override
    public void modifyLogLevel(Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(CaptionOCR.class.getPackageName()).setLevel(level);
    }

}
