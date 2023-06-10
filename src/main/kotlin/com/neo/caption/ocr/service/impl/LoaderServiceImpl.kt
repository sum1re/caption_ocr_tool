package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.annotation.Slf4j.Companion.log
import com.neo.caption.ocr.property.AppInfoProperties
import com.neo.caption.ocr.service.LoaderService
import org.bytedeco.javacpp.Loader
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.net.InetAddress
import kotlin.system.exitProcess

@Slf4j
@Service
class LoaderServiceImpl(
    private val appInfoProperties: AppInfoProperties,
    private val environment: Environment,
) : LoaderService {

    @Async
    override fun loadLib(vararg classes: Class<*>) {
        classes.forEach {
            Loader.load(it)
            log.info { "loading class: $it" }
        }
    }

    @Suppress("HttpUrlsUsage")
    @Async
    override fun printSystemInfo() {
        try {
            log.info {
                "system info: [" +
                        "platform=${Loader.getPlatform()}, " +
                        "chips=${Loader.totalChips()}, " +
                        "core=${Loader.totalCores()}, " +
                        "processors=${Loader.totalProcessors()}" +
                        "]"
            }
            log.info {
                "javacpp info: [" +
                        "cacheDir=${Loader.getCacheDir()}, " +
                        "tempDir=${Loader.getTempDir()}, " +
                        "version=${Loader.getVersion()}" +
                        "]"
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            log.error("Failed to get javacpp information, server will exit!!")
            exitProcess(-1)
        }
        log.info {
            "app info: [" +
                    "JavaVersion=${appInfoProperties.javaVersion}, " +
                    "SpringBootVersion=${appInfoProperties.springBootVersion}, " +
                    "appVersion=${appInfoProperties.version}, " +
                    "build=${appInfoProperties.buildTimestamp}" +
                    "]"
        }
        val ip = InetAddress.getLocalHost().hostAddress
        val port = environment.getProperty("server.port")
        log.info { "COCR server is ready! Open http://$ip:$port in your browser" }
    }

}