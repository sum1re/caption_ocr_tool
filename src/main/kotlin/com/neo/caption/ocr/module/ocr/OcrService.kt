package com.neo.caption.ocr.module.ocr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.neo.caption.ocr.common.CommonService
import com.neo.caption.ocr.common.TesseractProperties
import com.neo.caption.ocr.service.LoaderService
import com.neo.caption.ocr.support.convert
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

@CommonService
class OcrService(
    private val objectMapper: ObjectMapper,
    private val loaderService: LoaderService,
    private val tesseractProperties: TesseractProperties,
) {

    private val defaultOcrProfileName = "defaultTesseractProfile"

    // CRUD for OcrProfile
    fun insertOcrProfile(ocrProfileDto: OcrProfileDto) {
        OcrProfileTable.insert {
            it[this.name] = ocrProfileDto.name
            it[this.ocrType] = ocrProfileDto.ocrType
            it[this.config] = ocrProfileDto.config.stringify()
        }
    }

    fun insertDefaultOcrProfile() = insertOcrProfile(
        OcrProfileDto(
            name = defaultOcrProfileName,
            ocrType = OcrTypeEnum.TESSERACT,
            config = TesseractConfigDto(
                tesseractEngineEnum = tesseractProperties.ocrEngineMode,
                tesseractPageSegModeEnum = tesseractProperties.pageSegMode,
                language = tesseractProperties.language,
                vectors = tesseractProperties.vectors.associate { it.name to it.value },
            )
        )
    )

    fun findOcrProfileDtoList(): List<OcrProfileDto> = OcrProfileTable.selectAll().map {
        when (it[OcrProfileTable.ocrType]) {
            OcrTypeEnum.TESSERACT -> it.parse<TesseractConfigDto>()
            OcrTypeEnum.OCR_SPACE -> it.parse<OcrSpaceConfigDto>()
        }.run {
            OcrProfileDto(
                id = it[OcrProfileTable.id].value,
                name = it[OcrProfileTable.name],
                ocrType = it[OcrProfileTable.ocrType],
                config = this@run
            )
        }
    }

    fun findOcrProfileByOcrProfileId(ocrProfileId: UUID, nullable: Boolean = false): OcrProfile? {
        val result = OcrProfileTable.select(OcrProfileTable.ocrType, OcrProfileTable.config)
            .where { OcrProfileTable.id eq ocrProfileId }.singleOrNull()
        if (result == null && nullable) return null
        require(result != null) { "OcrProfile ID $ocrProfileId not found" }
        return result[OcrProfileTable.ocrType].run {
            when (this) {
                OcrTypeEnum.TESSERACT -> result.parse<TesseractConfigDto>().convert<TesseractConfig>()
                OcrTypeEnum.OCR_SPACE -> result.parse<OcrSpaceConfigDto>().convert<OcrSpaceConfig>()
            }.let { OcrProfile(this@run, it) }
        }
    }

    fun updateOcrProfile(ocrProfileId: UUID, newConfig: OcrConfigDto) {
        OcrProfileTable.select(OcrProfileTable.ocrType).where { OcrProfileTable.id eq ocrProfileId }
            .singleOrNull()?.get(OcrProfileTable.ocrType).run {
                when (this) {
                    OcrTypeEnum.TESSERACT -> require(newConfig is TesseractConfigDto) { "Invalid config type" }
                    OcrTypeEnum.OCR_SPACE -> require(newConfig is OcrSpaceConfigDto) { "Invalid config type" }
                    null -> throw RuntimeException("$ocrProfileId not found")
                }
            }
        OcrProfileTable.update({ OcrProfileTable.id eq ocrProfileId }) {
            it[this.config] = newConfig.stringify()
        }
    }

    fun deleteOcrProfileById(ocrProfileId: UUID) {
        OcrProfileTable.deleteWhere { id eq ocrProfileId }
    }

    fun deleteOcrProfileByName(name: String) {
        OcrProfileTable.deleteWhere { OcrProfileTable.name eq name }
    }

    fun getTesseractSupportLanguage(): List<String> = loaderService.javacpp().supportedLanguage

    fun resetDefaultOcrProfile() {
        deleteOcrProfileByName(defaultOcrProfileName)
        insertDefaultOcrProfile()
    }

    private fun OcrConfigDto.stringify() = objectMapper.writeValueAsString(this)

    private inline fun <reified T> ResultRow.parse() = objectMapper.readValue<T>(this[OcrProfileTable.config])

}