package com.neo.caption.ocr.domain

import com.fasterxml.jackson.annotation.JsonInclude

interface BaseData
interface BaseDto : BaseData

data class Pageable(
    val size: Int,
    val page: Int,
    val offset: Long = (size * page).toLong()
)

data class Page<T>(
    val content: List<T>,
    val elements: Int = content.size,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int = (totalElements / size + 1).toInt()
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommonResponse<T>(
    val success: Boolean,
    val errors: List<CommonError>,
    val result: T?,
    val resultInfo: CommonPagination?
) {
    constructor(result: T) : this(true, emptyList(), result, null)
    constructor(result: T, resultInfo: CommonPagination) : this(true, emptyList(), result, resultInfo)
    constructor(error: CommonError, vararg errors: CommonError) : this(false, listOf(error, *errors), null, null)
}

data class CommonError(val code: Int, val message: String)

data class CommonPagination(val count: Int, val page: Int, val perPage: Int, val totalCount: Long, val totalPage: Int)

typealias ErrorResponse = CommonResponse<Unit>