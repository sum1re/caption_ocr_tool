package com.neo.caption.ocr.exception;

import com.google.common.base.Throwables;
import com.neo.caption.ocr.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.neo.caption.ocr.domain.dto.BusinessErrorDto.buildResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleThrowable(Throwable e) {
        var logMsg = """
                unhandled throwable,
                cause: {}
                """;
        log.error(logMsg, Throwables.getStackTraceAsString(e));
        return buildResponse(ErrorCode.UNKNOWN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(@NotNull BusinessException e) {
        var logMsg = """
                business error code: {}
                message: {}
                cause: {}
                """;
        var errorCode = e.getErrorCode();
        log.warn(logMsg,
                errorCode.getCode(),
                errorCode.getMessage(),
                Throwables.getStackTraceAsString(e.getCause() == null ? e : e.getCause()));
        return buildResponse(e);
    }

}
