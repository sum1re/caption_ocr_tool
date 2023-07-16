package com.neo.caption.ocr.domain.mapper

import com.neo.caption.ocr.domain.dto.CropRangeDto
import com.neo.caption.ocr.domain.entity.CropRange
import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.math.min

@Component
class CropRangeMapper {

    fun toEntity(cropRangeDto: CropRangeDto, maxWidth: Int, maxHeight: Int): CropRange {
        return CropRange(
            min(abs(cropRangeDto.upperLeftX), maxWidth),
            min(abs(cropRangeDto.upperLeftY), maxHeight),
            min(abs(cropRangeDto.lowerRightX), maxWidth),
            min(abs(cropRangeDto.lowerRightY), maxHeight),
        )
    }

}