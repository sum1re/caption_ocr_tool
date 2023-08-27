package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.annotation.Slf4j.Companion.log
import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum
import com.neo.caption.ocr.domain.entity.TesseractConfig
import com.neo.caption.ocr.domain.vo.TesseractConfigVo
import com.neo.caption.ocr.domain.vo.TesseractOptionVo
import com.neo.caption.ocr.property.TesseractProperties
import com.neo.caption.ocr.service.TesseractService
import org.bytedeco.tesseract.TessBaseAPI
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

@Slf4j
@Service
@CacheConfig(cacheNames = ["tess::"])
class TesseractServiceImpl(
    private val tesseractProperties: TesseractProperties,
) : TesseractService {

    private val tessdataPath = Path.of("").resolve("lib").resolve("tessdata")
    override val supportedLanguage: List<String> by lazy {
        if (tessdataPath.notExists()) {
            log.warn { "not found tessdata in ${tessdataPath.absolutePathString()}" }
            emptyList()
        } else {
            Files.walk(tessdataPath)
                .filter { it.name.endsWith(".traineddata") }
                .map { it.nameWithoutExtension }
                .toList()
        }
    }

    @Cacheable("default-config")
    override fun getDefaultConfig(): TesseractConfigVo {
        return TesseractConfigVo(
            tesseractProperties.ocrEngineMode,
            tesseractProperties.pageSegMode,
            tesseractProperties.language,
            tesseractProperties.linkedHashMap()
        )
    }

    @Cacheable(key = "'option'")
    override fun getTesseractOption() =
        TesseractOptionVo(
            oemModeList = OCREngineModeEnum.values().toList(),
            psmModeList = PageSegModeEnum.values().toList(),
            supportedLanguage = supportedLanguage,
            vectorKeyList = emptyList()
        )

    override fun initTessBaseApi(tesseractConfig: TesseractConfig): TessBaseAPI {
        return TessBaseAPI().apply {
            this.Init(
                tessdataPath.absolutePathString(),
                tesseractConfig.language,
                tesseractConfig.oem,
                ByteArray(0),
                tesseractConfig.vectorKey.size().toInt(),
                tesseractConfig.vectorKey,
                tesseractConfig.vectorValue,
                true
            )
        }
    }

}
