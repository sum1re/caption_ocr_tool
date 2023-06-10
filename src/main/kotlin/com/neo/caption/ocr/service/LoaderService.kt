package com.neo.caption.ocr.service

interface LoaderService {

    fun loadLib(vararg classes: Class<*>)

    fun printSystemInfo()

}