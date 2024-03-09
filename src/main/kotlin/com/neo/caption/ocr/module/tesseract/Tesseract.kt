package com.neo.caption.ocr.module.tesseract

import com.neo.caption.ocr.common.languageSeparator
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.domain.BaseEntity
import com.neo.caption.ocr.service.LoaderService
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import org.bytedeco.tesseract.StringVector
import org.springframework.core.convert.converter.Converter
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Entity
data class AppTesseractConfig(
    val ocrEngineMode: OCREngineModeEnum = OCREngineModeEnum.OEM_LSTM_ONLY,
    val pageSegMode: PageSegModeEnum = PageSegModeEnum.PSM_SINGLE_LINE,
    val language: String = "",
    @OneToMany @JoinColumn val vectors: List<AppTesseractVector> = emptyList(),
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
) : BaseEntity

@Entity
data class AppTesseractVector(
    val key: String = "",
    val value: String = "",
    @Id @GeneratedValue var id: Long? = null,
) : BaseEntity

/**
 * Hold global configuration for Tesseract.
 *
 * @param selectedOcrEngineMode The currently selected OCR engine mode.
 * @param ocrEngineModeList A list of available OCR engine modes.
 * @param selectedPageSegModeEnum The currently selected page segmentation mode.
 * @param pageSegModeList A list of available page segmentation modes.
 * @param selectedLanguage A list of selected languages for OCR.
 * @param supportedLanguages A list of supported languages by the OCR engine.
 * @param vectors A map of string key-value pairs, additional configuration.
 */
data class AppTesseractConfigDto(
    val selectedOcrEngineMode: OCREngineModeEnum,
    val ocrEngineModeList: List<OCREngineModeEnum>,
    val selectedPageSegModeEnum: PageSegModeEnum,
    val pageSegModeList: List<PageSegModeEnum>,
    val selectedLanguage: List<String>,
    val supportedLanguages: List<String>,
    val vectors: Map<String, String>
) : BaseDto

/**
 * A specific configuration for the Tesseract, used in single project.
 */
data class TesseractConfig(
    val ocrEngineMode: Int,
    val language: String,
    val vectorKey: StringVector,
    val vectorValue: StringVector,
)

data class TesseractConfigDto(
    val ocrEngineMode: OCREngineModeEnum?,
    val pageSegMode: PageSegModeEnum?,
    val selectedLanguage: List<String>?,
    val vectors: Map<String, String>?
) : BaseDto

@Repository
interface AppTesseractConfigRepo : CrudRepository<AppTesseractConfig, UUID>

@Repository
interface AppTesseractVectorRepo : CrudRepository<AppTesseractVector, Long> {
    fun deleteByKeyIn(keyList: List<String>)
}

@Component
class AppTesseractConfigConvert(
    private val loaderService: LoaderService
) : Converter<AppTesseractConfig, AppTesseractConfigDto> {
    override fun convert(source: AppTesseractConfig) = AppTesseractConfigDto(
        selectedOcrEngineMode = source.ocrEngineMode,
        ocrEngineModeList = OCREngineModeEnum.entries,
        selectedPageSegModeEnum = source.pageSegMode,
        pageSegModeList = PageSegModeEnum.entries,
        selectedLanguage = source.language.split(languageSeparator),
        supportedLanguages = loaderService.javacpp().supportedLanguage,
        vectors = source.vectors.associate { it.key to it.value }
    )
}

@Component
class AppTesseractConfigDtoToAppTesseractConfigConverter : Converter<AppTesseractConfigDto, AppTesseractConfig> {
    override fun convert(source: AppTesseractConfigDto) = AppTesseractConfig(
        ocrEngineMode = source.selectedOcrEngineMode,
        pageSegMode = source.selectedPageSegModeEnum,
        language = source.selectedLanguage.joinToString(languageSeparator),
        vectors = source.vectors.toVectorList()
    )
}

@Component
class TesseractConfigDtoToTesseractConfigConverter(
    private val appTesseractService: AppTesseractService
) : Converter<TesseractConfigDto, TesseractConfig> {
    override fun convert(source: TesseractConfigDto): TesseractConfig {
        val appTesseractConfig = appTesseractService.appTesseractConfig
        val vectorKey = StringVector()
        val vectorValue = StringVector()
        (appTesseractConfig.vectors + source.vectors.toVectorList()).forEach {
            vectorKey.put(it.key)
            vectorValue.put(it.value)
        }
        (source.pageSegMode ?: appTesseractConfig.pageSegMode).toVector().also {
            vectorKey.put(it.first)
            vectorValue.put(it.second)
        }
        return TesseractConfig(
            ocrEngineMode = (source.ocrEngineMode ?: appTesseractConfig.ocrEngineMode).code,
            language = source.selectedLanguage?.joinToString(languageSeparator) ?: appTesseractConfig.language,
            vectorKey = vectorKey,
            vectorValue = vectorValue,
        )
    }
}

private fun Map<String, String>?.toVectorList(): List<AppTesseractVector> {
    return this.takeUnless { it.isNullOrEmpty() }
        ?.let { m -> m.map { AppTesseractVector(it.key, it.value) } }
        ?: emptyList()
}
