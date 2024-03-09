package com.neo.caption.ocr.module.tesseract

import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.CACHE_TESS_CONFIG
import com.neo.caption.ocr.common.CacheableService
import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.TesseractProperties
import com.neo.caption.ocr.common.languageSeparator
import com.neo.caption.ocr.service.LoaderService
import jakarta.annotation.PostConstruct
import org.bytedeco.tesseract.TessBaseAPI
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@CacheableService
class AppTesseractService(
    private val loaderService: LoaderService,
    private val tesseractProperties: TesseractProperties,
    private val configRepo: AppTesseractConfigRepo,
    private val vectorRepo: AppTesseractVectorRepo,
) {

    private val apiMap: MutableMap<String, TessBaseAPI> = mutableMapOf()

    lateinit var appTesseractConfig: AppTesseractConfig
        @Cacheable(key = "'${CACHE_TESS_CONFIG}default'") get

    @PostConstruct
    private fun init() {
        appTesseractConfig = configRepo.findById(tesseractProperties.uuid)
            .getOrNull().takeIf { it != null }
            ?: configRepo.save(defaultConfig())
    }

    @CachePut(key = "'${CACHE_TESS_CONFIG}default'")
    fun updateTesseractConfig(target: AppTesseractConfig): AppTesseractConfig {
        val source = configRepo.findById(tesseractProperties.uuid).getOrElse {
            throw IllegalArgumentException("Tesseract not found by id (${tesseractProperties.uuid})")
        }
        return configRepo.save(target.copy(id = source.id)).apply {
            val targetVectorKey = this.vectors.map { it.key }
            source.vectors.filterNot { it.key in targetVectorKey }.map { it.key }
                .run { vectorRepo.deleteByKeyIn(this) }
        }
    }

    @CachePut(key = "'${CACHE_TESS_CONFIG}default'")
    fun resetConfig() =
        configRepo.deleteById(tesseractProperties.uuid).let { configRepo.save(defaultConfig()) }

    fun initialTessBaseApi(projectId: String, config: TesseractConfig): TessBaseAPI {
        val api = TessBaseAPI()
        api.Init(
            loaderService.javacpp().tessdataDir,
            config.language,
            config.ocrEngineMode,
            ByteArray(0),
            config.vectorKey.size().toInt(),
            config.vectorKey,
            config.vectorValue,
            true
        )
        apiMap[projectId] = api
        return api
    }

    fun getTessBaseApi(projectId: String) =
        apiMap[projectId] ?: throw BadRequestException(ErrorCodeEnum.INVALID_PARAMETER, "failed to invoke tesseract")

    @CacheEvict(key = "'$CACHE_TESS_CONFIG' + #p0")
    fun closeTessBaseApi(projectId: String) {
        apiMap[projectId]?.run {
            this.releaseReference()
            apiMap.remove(projectId)
        }
    }

    private fun defaultConfig() = AppTesseractConfig(
        ocrEngineMode = tesseractProperties.ocrEngineMode,
        pageSegMode = tesseractProperties.pageSegMode,
        language = tesseractProperties.language.joinToString(languageSeparator),
        vectors = tesseractProperties.vectors.map { AppTesseractVector(it.name, it.value) },
        id = tesseractProperties.uuid
    )

    // TODO: save each project config into database?
}