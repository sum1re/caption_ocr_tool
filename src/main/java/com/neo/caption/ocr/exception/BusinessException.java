package com.neo.caption.ocr.exception;

import com.neo.caption.ocr.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7828399674568412226L;

    private final ErrorCode errorCode;
    private final Throwable cause;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

}
