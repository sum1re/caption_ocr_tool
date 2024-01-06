package com.neo.caption.ocr.module.tesseract

import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.TesseractProperties
import com.neo.caption.ocr.common.languageSeparator
import jakarta.annotation.PostConstruct
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@Slf4j
@Service
@CacheConfig(cacheNames = ["app::tess"])
class AppTesseractService(
    private val tesseractProperties: TesseractProperties,
    private val appTesseractConfigRepo: AppTesseractConfigRepo,
) {

    lateinit var appTesseractConfig: AppTesseractConfig
        @Cacheable("option") get

    @PostConstruct
    private fun init() {
        appTesseractConfig = appTesseractConfigRepo.findById(tesseractProperties.uuid)
            .getOrNull().takeIf { it != null }
            ?: appTesseractConfigRepo.save(defaultConfig())
    }

    @CachePut("option")
    fun updateTesseractConfig(tesseractConfigDto: TesseractConfigDto): AppTesseractConfig {
        val source = appTesseractConfigRepo.findById(tesseractProperties.uuid).getOrElse {
            throw IllegalArgumentException("Tesseract not found by id (${tesseractProperties.uuid})")
        }
        val mutableVectors = source.vectors.toMutableList()
        val vectorKeys = source.vectors.map { it.key }
        vectorKeys.forEach {
            // remove deleted
            if (it !in tesseractConfigDto.vectors.keys) {
                mutableVectors.remove(source.vectors.first { i -> i.key == it })
            }
        }
        tesseractConfigDto.vectors.forEach {
            // add new
            if (it.key !in vectorKeys) {
                mutableVectors.add(AppTesseractVector(it.key, it.value))
            }
        }
        val patched = AppTesseractConfig(
            ocrEngineMode = tesseractConfigDto.selectedOcrEngineMode,
            pageSegMode = tesseractConfigDto.selectedPageSegModeEnum,
            language = tesseractConfigDto.selectedLanguage.joinToString(languageSeparator),
            vectors = mutableVectors,
            id = source.id
        )
        return appTesseractConfigRepo.save(patched)
    }

    @CachePut("option")
    fun resetConfig() =
        appTesseractConfigRepo.deleteById(tesseractProperties.uuid).let { appTesseractConfigRepo.save(defaultConfig()) }

    private fun defaultConfig() = AppTesseractConfig(
        ocrEngineMode = tesseractProperties.ocrEngineMode,
        pageSegMode = tesseractProperties.pageSegMode,
        language = tesseractProperties.language.joinToString(languageSeparator),
        vectors = tesseractProperties.vectors.map { AppTesseractVector(it.name, it.value) },
        id = tesseractProperties.uuid
    )

}