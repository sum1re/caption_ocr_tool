package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.entity.TesseractConfig
import org.opencv.core.Mat

interface OCRService {

    fun initial(tesseractConfig: TesseractConfig, identity: String)

    fun release(identity: String)

    fun doOCR(identity: String, mat: Mat): String

}