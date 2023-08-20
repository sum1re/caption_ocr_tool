package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.entity.TesseractConfig
import com.neo.caption.ocr.domain.vo.TesseractConfigVo
import org.bytedeco.tesseract.TessBaseAPI

interface TesseractService {

    val supportedLanguage: List<String>

    fun getDefaultConfig(): TesseractConfigVo

    fun initTessBaseApi(tesseractConfig: TesseractConfig): TessBaseAPI

}