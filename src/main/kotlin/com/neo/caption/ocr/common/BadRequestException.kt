package com.neo.caption.ocr.common

import org.springframework.http.HttpStatus

class BadRequestException(
    val code: ErrorCodeEnum,
    override val message: String = code.message,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException()