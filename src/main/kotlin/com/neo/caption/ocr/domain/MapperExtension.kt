package com.neo.caption.ocr.domain

import com.neo.caption.ocr.domain.dto.CropRangeDto
import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.TesseractConfigDto
import com.neo.caption.ocr.domain.entity.*
import com.neo.caption.ocr.domain.vo.CaptionRowVo
import com.neo.caption.ocr.domain.vo.TaskVo
import com.neo.caption.ocr.toEncodeByteArray
import org.bytedeco.tesseract.StringVector
import java.math.BigDecimal
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.math.min

@OptIn(ExperimentalEncodingApi::class)
fun CaptionRow.toVo(frameDuration: BigDecimal) =
    CaptionRowVo(
        start = this.start.let { if (it == 0) BigDecimal.ZERO else it.subtract(frameDuration) },
        end = this.end.add(frameDuration),
        imgData = Base64.encode(this.mat.toEncodeByteArray()),
        caption = this.caption
    )

fun List<CaptionRow>.toVo(frameDuration: BigDecimal) =
    this.map { it.toVo(frameDuration) }.toList()

fun CropRangeDto.toEntity(maxWidth: Int, maxHeight: Int) =
    CropRange(
        upperLeftX = min(abs(this.upperLeftX), maxWidth),
        upperLeftY = min(abs(this.upperLeftY), maxHeight),
        lowerRightX = min(abs(this.lowerRightX), maxWidth),
        lowerRightY = min(abs(this.lowerRightY), maxHeight),
    )

fun FileChecksumDto.toEntity(projectId: String) =
    FileChecksum(
        hash = this.hash,
        projectId = projectId,
        extension = this.extension,
        fileChunkName = this.fileChunkName
    )

fun TesseractConfigDto.toEntity(): TesseractConfig {
    val vectorKey = StringVector()
    val vectorValue = StringVector()
    vectorKey.put("tessedit_pageseg_mode")
    vectorValue.put(this.psmMode.code.toString())
    this.vectors.entries.forEach {
        vectorKey.put(it.key)
        vectorValue.put(it.value)
    }
    return TesseractConfig(
        oem = this.oemMode.code,
        language = this.language.joinToString("+"),
        vectorKey = vectorKey,
        vectorValue = vectorValue
    )
}

fun TaskConfig.toVo() =
    TaskVo(
        taskId = this.taskId,
        totalFrame = this.videoInfo!!.totalFrame
    )

private fun Int.add(frameDuration: BigDecimal) =
    BigDecimal(this.toString()).add(BigDecimal("0.5")).multiply(frameDuration)

private fun Int.subtract(frameDuration: BigDecimal) =
    BigDecimal(this.toString()).subtract(BigDecimal("0.5")).multiply(frameDuration)