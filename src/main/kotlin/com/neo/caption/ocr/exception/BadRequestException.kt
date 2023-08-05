package com.neo.caption.ocr.exception

import com.neo.caption.ocr.constant.ErrorCodeEnum
import org.springframework.http.HttpStatus

class BadRequestException(
    val code: Int,
    override val message: String,
    val httpStatus: HttpStatus
) : RuntimeException() {

    constructor(message: String) : this(ErrorCodeEnum.DEFAULT_BAD_REQUEST, message)

    constructor(errorCode: ErrorCodeEnum, message: String = errorCode.message) : this(
        errorCode.code, message, errorCode.httpStatus
    )

}