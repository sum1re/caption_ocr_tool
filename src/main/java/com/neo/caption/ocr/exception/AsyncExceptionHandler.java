package com.neo.caption.ocr.exception;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(@NotNull Throwable ex, @NotNull Method method, Object @NotNull ... params) {
        var logMsg = """
                uncaught exception in thread
                method: {}
                params: {}
                message: {}
                """;
        log.error(logMsg, method, params, Throwables.getStackTraceAsString(ex));
    }

}
