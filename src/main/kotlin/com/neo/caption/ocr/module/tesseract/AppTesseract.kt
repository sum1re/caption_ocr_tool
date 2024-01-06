package com.neo.caption.ocr.module.tesseract

import com.neo.caption.ocr.common.languageSeparator
import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.domain.BaseEntity
import com.neo.caption.ocr.service.LoaderService
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import org.springframework.core.convert.converter.Converter
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Entity
class AppTesseractConfig(
    val ocrEngineMode: OCREngineModeEnum = OCREngineModeEnum.OEM_LSTM_ONLY,
    val pageSegMode: PageSegModeEnum = PageSegModeEnum.PSM_SINGLE_LINE,
    val language: String = "",
    @OneToMany @JoinColumn val vectors: List<AppTesseractVector> = emptyList(),
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
) : BaseEntity

@Entity
class AppTesseractVector(
    @Column(unique = true) val key: String = "",
    val value: String = "",
    @Id @GeneratedValue var id: Long? = null,
) : BaseEntity

@Repository
interface AppTesseractConfigRepo : CrudRepository<AppTesseractConfig, UUID>

data class TesseractConfigDto(
    val selectedOcrEngineMode: OCREngineModeEnum,
    val ocrEngineModeList: List<OCREngineModeEnum>,
    val selectedPageSegModeEnum: PageSegModeEnum,
    val pageSegModeList: List<PageSegModeEnum>,
    val selectedLanguage: List<String>,
    val supportedLanguages: List<String>,
    val vectors: Map<String, String>
) : BaseDto

@Component
class AppTesseractConfigConvert(
    private val loaderService: LoaderService
) : Converter<AppTesseractConfig, TesseractConfigDto> {
    override fun convert(source: AppTesseractConfig) = TesseractConfigDto(
        selectedOcrEngineMode = source.ocrEngineMode,
        ocrEngineModeList = OCREngineModeEnum.entries,
        selectedPageSegModeEnum = source.pageSegMode,
        pageSegModeList = PageSegModeEnum.entries,
        selectedLanguage = source.language.split(languageSeparator),
        supportedLanguages = loaderService.javacpp().supportedLanguage,
        vectors = source.vectors.associate { it.key to it.value }
    )
}

