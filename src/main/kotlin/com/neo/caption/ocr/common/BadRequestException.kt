package com.neo.caption.ocr.common

import com.neo.caption.ocr.constant.ErrorCodeEnum
import org.springframework.http.HttpStatus

class BadRequestException(
    val code: ErrorCodeEnum,
    override val message: String,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException()