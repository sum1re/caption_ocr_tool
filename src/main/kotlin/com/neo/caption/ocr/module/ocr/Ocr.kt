package com.neo.caption.ocr.module.ocr

import com.neo.caption.ocr.common.languageSeparator
import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import org.bytedeco.tesseract.StringVector
import org.jetbrains.exposed.dao.id.UUIDTable
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.UUID

interface OcrConfig : BaseData
interface OcrConfigDto : BaseDto

object OcrProfileTable : UUIDTable("OCR_PROFILE") {
    val name = varchar("FRIENDLY_NAME", length = 255).uniqueIndex()
    val ocrType = customEnumeration(
        name = "OCR_TYPE",
        sql = "ENUM(${OcrTypeEnum.entries.joinToString(",") { "'$it'" }})",
        fromDb = { OcrTypeEnum.valueOf(it as String) },
        toDb = { it.name }
    )
    val config = text("OCR_CONFIG") // NOTE: This column stringify by OcrConfigDto
}

data class OcrProfile(
    val ocrType: OcrTypeEnum,
    val config: OcrConfig,
)

data class OcrProfileDto(
    val id: UUID = UUID(0, 0),
    val name: String,
    val ocrType: OcrTypeEnum,
    val config: OcrConfigDto,
)

data class TesseractConfig(
    val engine: Int,
    val language: String,
    val vectorKey: StringVector,
    val vectorValue: StringVector,
) : OcrConfig

data class TesseractConfigDto(
    val tesseractEngineEnum: TesseractEngineEnum,
    val tesseractPageSegModeEnum: TesseractPageSegModeEnum,
    val language: List<String>,
    val vectors: Map<String, String>
) : OcrConfigDto

data class OcrSpaceConfig(
    val apiKey: String,
    val language: String,
    val isOverlayRequired: Boolean,
    val engine: Int
) : OcrConfig

data class OcrSpaceConfigDto(
    val apiKey: String,
    val language: String,
    val isOverlayRequired: Boolean,
    val ocrSpaceEngineEnum: OcrSpaceEngineEnum
) : OcrConfigDto

@Component
class TesseractConfigDtoToTesseractConfigConverter : Converter<TesseractConfigDto, TesseractConfig> {
    override fun convert(source: TesseractConfigDto): TesseractConfig {
        val vectorKey = StringVector(*source.vectors.keys.toTypedArray())
        val vectorValue = StringVector(*source.vectors.values.toTypedArray())
        vectorKey.put("tessedit_pageseg_mode")
        vectorValue.put(source.tesseractPageSegModeEnum.code.toString())
        return TesseractConfig(
            engine = source.tesseractEngineEnum.code,
            language = source.language.joinToString(languageSeparator),
            vectorKey = vectorKey,
            vectorValue = vectorValue
        )
    }
}

@Component
class OcrSpaceConfigDtoToOcrSpaceConfigConverter : Converter<OcrSpaceConfigDto, OcrSpaceConfig> {
    override fun convert(source: OcrSpaceConfigDto) = OcrSpaceConfig(
        apiKey = source.apiKey,
        language = source.language,
        isOverlayRequired = source.isOverlayRequired,
        engine = source.ocrSpaceEngineEnum.code
    )
}

