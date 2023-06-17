package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.dto.TesseractConfigDto
import org.opencv.core.Mat

interface OCRService {

    fun initial(tesseractConfigDto: TesseractConfigDto, identity: String)

    fun doOCR(identity: String, mat: Mat): String

}