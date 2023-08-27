package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.entity.TesseractConfig
import org.opencv.core.Mat

interface OCRService {

    fun initial(tesseractConfig: TesseractConfig, projectId: String)

    fun release(projectId: String)

    fun doOCR(projectId: String, mat: Mat): String

}