package com.neo.caption.ocr.domain

import com.fasterxml.jackson.annotation.JsonInclude

interface BaseDto
interface BaseEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RestVo<T>(
    val success: Boolean,
    val errors: List<RestErrorVo>,
    val result: T?,
    val resultInfo: RestPageVo?
) {
    constructor(result: T) : this(true, emptyList(), result, null)
    constructor(result: T, resultInfo: RestPageVo) : this(true, emptyList(), result, resultInfo)
    constructor(error: RestErrorVo, vararg errors: RestErrorVo) : this(false, listOf(error, *errors), null, null)
}

data class RestErrorVo(val code: Int, val message: String)

data class RestPageVo(val count: Int, val size: Int, val totalPage: Int, val totalElements: Long)

typealias ErrorRest = RestVo<Unit>