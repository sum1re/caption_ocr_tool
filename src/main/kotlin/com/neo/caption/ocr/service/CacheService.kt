package com.neo.caption.ocr.service

import com.neo.caption.ocr.constant.CachePrefixEnum

interface CacheService {

    fun <T> getCache(prefix: CachePrefixEnum, key: String): T?

    fun <T> putCache(prefix: CachePrefixEnum, key: String, data: T): T

    fun removeCache(prefix: CachePrefixEnum, key: String)

}