package com.neo.caption.ocr.common

import com.yomahub.liteflow.exception.LiteFlowException
import org.springframework.http.HttpStatus

class BadRequestException(
    val code: ErrorCodeEnum,
    override val message: String = code.message,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException()

fun throwLiteFlowException(code: ErrorCodeEnum, message: String = code.message): Nothing {
    throw LiteFlowException(code.name, message)
}