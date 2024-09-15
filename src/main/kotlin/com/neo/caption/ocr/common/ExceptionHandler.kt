package com.neo.caption.ocr.common

import com.neo.caption.ocr.common.Slf4j.Companion.logging
import com.neo.caption.ocr.domain.ErrorResponse
import org.apache.catalina.connector.ClientAbortException
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.lang.reflect.Method

@Slf4j
class CaffeineCacheErrorHandler : CacheErrorHandler {

    override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
        logging.error { "CacheError [option: GET, key: $key, cause: ${exception.cause}]" }
    }

    override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) {
        logging.error { "CacheError [option: PUT, key: $key, value: $value, cause: ${exception.cause}]" }
    }

    override fun handleCacheEvictError(exception: RuntimeException, cache: Cache, key: Any) {
        logging.error { "CacheError [option: EVICT, key: $key, cause: ${exception.cause}]" }
    }

    override fun handleCacheClearError(exception: RuntimeException, cache: Cache) {
        logging.error { "CacheError [option: CLEAR, cause: ${exception.cause}]" }
    }

}

@Slf4j
@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException) = e.handleException(e.code, e.message, e.httpStatus)

    @ExceptionHandler(value = [ClientAbortException::class, NoResourceFoundException::class])
    fun handleClientAbort(e: Throwable) {
        // ClientAbortException caused by static video file, ignore it
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(e: Throwable) = e.handleException(ErrorCodeEnum.UNKNOWN_ERROR)

    private fun Throwable.handleException(
        code: ErrorCodeEnum,
        message: String = code.message,
        status: HttpStatus = HttpStatus.BAD_REQUEST
    ): ResponseEntity<ErrorResponse> {
        this.printStackTrace()
        return ResponseEntity(code.toResponse(message), status)
    }

}

@Slf4j
class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {

    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any) {
        logging.error { "async exception: [method: $method, params: $params, message: ${ex.stackTraceToString()}]" }
        throw ex
    }

}