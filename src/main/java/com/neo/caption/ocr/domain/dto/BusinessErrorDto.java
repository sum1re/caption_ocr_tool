package com.neo.caption.ocr.domain.dto;

import com.neo.caption.ocr.constant.ErrorCode;
import com.neo.caption.ocr.exception.BusinessException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public record BusinessErrorDto(Long date, Integer code, String message) {

    public static @NotNull ResponseEntity<Object> buildResponse(@NotNull ErrorCode errorCode) {
        var dto = new BusinessErrorDto(Instant.now().toEpochMilli(), errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(dto, errorCode.getHttpStatus());
    }

    public static @NotNull ResponseEntity<Object> buildResponse(@NotNull BusinessException businessException) {
        return buildResponse(businessException.getErrorCode());
    }

}
