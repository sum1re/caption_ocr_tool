package com.neo.caption.ocr.domain.mapper

import com.neo.caption.ocr.domain.dto.TesseractConfigDto
import com.neo.caption.ocr.domain.entity.TesseractConfig
import org.bytedeco.tesseract.StringVector
import org.springframework.stereotype.Component

@Component
class TesseractConfigMapper {

    fun toEntity(tesseractConfigDto: TesseractConfigDto): TesseractConfig {
        val vectorKey = StringVector()
        val vectorValue = StringVector()
        vectorKey.put("tessedit_pageseg_mode")
        vectorValue.put(tesseractConfigDto.psmMode.code.toString())
        tesseractConfigDto.vectors.entries.forEach {
            vectorKey.put(it.key)
            vectorValue.put(it.value)
        }
        return TesseractConfig(
            tesseractConfigDto.oemMode.code,
            tesseractConfigDto.language.joinToString("+"),
            vectorKey,
            vectorValue
        )
    }

}