package com.neo.caption.ocr.domain.mapper

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.domain.entity.CaptionRow
import com.neo.caption.ocr.domain.vo.CaptionRowVo
import com.neo.caption.ocr.toEncodeByteArray
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Slf4j
@Component
class CaptionRowMapper {

    @OptIn(ExperimentalEncodingApi::class)
    fun toVo(captionRow: CaptionRow, frameDuration: BigDecimal): CaptionRowVo {
        val start = captionRow.start.let { if (it == 0) BigDecimal.ZERO else it.subtract(frameDuration) }
        val end = captionRow.end.add(frameDuration)
        val data = Base64.encode(captionRow.mat.toEncodeByteArray())
        return CaptionRowVo(start, end, data, captionRow.caption)
    }

    fun toVo(captionRowList: List<CaptionRow>, frameDuration: BigDecimal) =
        captionRowList.map { toVo(it, frameDuration) }.toList()

    private fun Int.add(frameDuration: BigDecimal) =
        BigDecimal(this.toString()).add(BigDecimal("0.5")).multiply(frameDuration)

    private fun Int.subtract(frameDuration: BigDecimal) =
        BigDecimal(this.toString()).subtract(BigDecimal("0.5")).multiply(frameDuration)
}