package com.neo.caption.ocr.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_ARGUMENT(100, HttpStatus.BAD_REQUEST, "The argument is invalid"),
    FAILED_PRECONDITION(101, HttpStatus.BAD_REQUEST, "Request can not be executed in the current system state"),
    OUT_OF_RANGE(102, HttpStatus.BAD_REQUEST, "Specified an invalid range"),
    UNAUTHENTICATED(103, HttpStatus.UNAUTHORIZED, "You are missing authentication information."),
    PERMISSION_DENIED(104, HttpStatus.PERMANENT_REDIRECT, "You have no permission to access the resource"),
    ABORTED(105, HttpStatus.CONFLICT, "IO exception"),
    ALREADY_EXISTS(106, HttpStatus.CONFLICT, "The file or directory already exists"),
    DATA_LOSS(107, HttpStatus.GONE, "Data may have been lost or corrupted"),
    UNKNOWN(108, HttpStatus.INTERNAL_SERVER_ERROR, "The server has some problem, you can try again later or report the issue in Github.");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

}
