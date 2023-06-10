package com.neo.caption.ocr.handler

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.constant.ErrorCodeEnum
import com.neo.caption.ocr.domain.vo.RestErrorVo
import com.neo.caption.ocr.domain.vo.RestVo
import com.neo.caption.ocr.exception.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

@Slf4j
@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException) = RestErrorVo(e.code, e.message).toResponse(e.httpStatus)

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleEntityNotFound(e: NoHandlerFoundException) = ErrorCodeEnum.INVALID_URL.toResponse(e.message)

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(e: Throwable) = e.let {
        e.printStackTrace()
        ErrorCodeEnum.UNKNOWN_ERROR.toResponse(e.message)
    }

    private fun RestErrorVo.toResponse(httpStatus: HttpStatus) = ResponseEntity(RestVo<Any>(this), httpStatus)

    private fun ErrorCodeEnum.toResponse(message: String?) = this.toRestError(message).toResponse(this.httpStatus)

}