package com.neo.caption.ocr.constant

import com.neo.caption.ocr.domain.vo.RestErrorVo
import org.springframework.http.HttpStatus

enum class ErrorCodeEnum(val code: Int, val message: String, val httpStatus: HttpStatus) {

    /**
     * code: 1xxx
     * usually cause by user
     */
    BANNED_ACCOUNT(1006, "Your account has benn banned", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1007, "You do not have permission to access", HttpStatus.FORBIDDEN),
    INVALID_URL(1008, "404", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER(1034, "invalid param", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1200, "file is broken", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_FOUND(1201, "video file not found", HttpStatus.BAD_REQUEST),
    DEFAULT_BAD_REQUEST(1999, "default error", HttpStatus.BAD_REQUEST),

    /**
     * code: 10xxxx
     * usually cause by service
     */
    SERVER_IO_ERROR(10100, "IO Error", HttpStatus.INTERNAL_SERVER_ERROR),
    LOSS_TESSERACT_DATA(10101, "miss tess data", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_READ_ERROR(10102, "failed to open video file", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(10999, "issue to sum1re/caption_ocr_tool", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    fun toRestError(message: String?) = RestErrorVo(this.code, message ?: this.message)

}