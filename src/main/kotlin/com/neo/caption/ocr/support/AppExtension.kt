package com.neo.caption.ocr.support

import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.CommonPagination
import com.neo.caption.ocr.domain.CommonResponse
import jakarta.annotation.PostConstruct
import org.springframework.core.convert.ConversionService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Slf4j
@Component
class AppExtension(
    val conversionService: ConversionService,
) {
    @PostConstruct
    private fun init() {
        instant = this
    }
}

lateinit var instant: AppExtension

inline fun <reified T> BaseData.convert(): T = instant.conversionService.convert(this, T::class.java)!!

fun <T> response(action: () -> T?): CommonResponse<T?> =
    when (val result = action()) {
        is Collection<*> -> CommonResponse(result, result.toPagination())
        is Page<*> -> CommonResponse(result, result.toPagination())
        else -> CommonResponse(result)
    }

private fun Collection<*>.toPagination() = this.size.let { CommonPagination(it, 0, it, it.toLong(), 1) }

private fun Page<*>.toPagination() =
    CommonPagination(
        count = this.numberOfElements,
        page = this.size,
        perPage = this.number,
        totalCount = this.totalElements,
        totalPage = this.totalPages
    )

inline fun <T, R : Comparable<R>> Collection<T>.findMinBy(crossinline selector: (T) -> R): T? {
    if (this.isEmpty()) return null
    return this.sortedWith(compareBy(selector)).first()
}

inline fun <T, R : Comparable<R>> Collection<T>.findMaxBy(crossinline selector: (T) -> R): T? {
    if (this.isEmpty()) return null
    return this.sortedWith(compareByDescending(selector)).first()
}

inline fun <T, R : Comparable<R>> Collection<T>.findMidBy(crossinline selector: (T) -> R): T? {
    if (this.isEmpty()) return null
    val index = if (this.size % 2 == 0) this.size / 2 else this.size / 2 + 1
    return this.sortedWith(compareBy(selector))[index]
}
