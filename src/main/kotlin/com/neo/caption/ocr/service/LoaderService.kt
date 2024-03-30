package com.neo.caption.ocr.service

import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.Slf4j.Companion.log
import org.bytedeco.javacpp.Loader
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.system.exitProcess

data class SystemInformation(
    val platform: String,
    val chips: Int,
    val core: Int,
    val processors: Int,
    val ip: String,
    val port: Int
)

data class JavacppInformation(
    val version: String,
    val cacheDir: String,
    val tempDir: String,
    val tessdataDir: String,
    val supportedLanguage: List<String>,
)

@Slf4j
@Service
@CacheConfig(cacheNames = ["system"])
class LoaderService(
    private val serverProperties: ServerProperties,
) {

    @Async
    fun loadLib(vararg classes: Class<*>): Unit = classes.forEach { it.loading() }

    @Async
    @Cacheable("information")
    fun systemInformation(): SystemInformation = SystemInformation(
        platform = Loader.getPlatform(),
        chips = Loader.totalChips(),
        core = Loader.totalCores(),
        processors = Loader.totalProcessors(),
        ip = serverProperties.address.hostAddress,
        port = serverProperties.port
    )

    @Async
    @Cacheable("javacpp")
    fun javacpp(): JavacppInformation = runCatching {
        val tessdataPath = Path("lib", "tessdata")
        JavacppInformation(
            version = Loader.getVersion(),
            cacheDir = Loader.getCacheDir().absolutePath,
            tempDir = Loader.getTempDir().absolutePath,
            tessdataDir = tessdataPath.absolutePathString(),
            supportedLanguage = tessdataPath.takeIf { it.exists() }?.run {
                Files.walk(this).filter { it.name.endsWith(".traineddata") }.map { it.nameWithoutExtension }.toList()
            } ?: emptyList()
        )
    }.getOrElse {
        log.error("Application will exit because failed loading javacpp library", it)
        exitProcess(-1)
    }

    private fun Class<*>.loading() {
        Loader.load(this)
        log.debug { "Loading class: $this" }
    }

}