package com.neo.caption.ocr.support

import com.neo.caption.ocr.common.COCR_BATCH_OCR_CHAIN
import com.neo.caption.ocr.common.COCR_PROJECT_CHAIN
import com.neo.caption.ocr.common.CommonProperties
import com.neo.caption.ocr.common.Slf4j.Companion.logging
import com.neo.caption.ocr.module.app.AppConfigTable
import com.neo.caption.ocr.module.app.AppService
import com.neo.caption.ocr.module.liteflow.AstEdgeTable
import com.neo.caption.ocr.module.liteflow.AstEntityTable
import com.neo.caption.ocr.module.liteflow.AstModelTable
import com.neo.caption.ocr.module.liteflow.LiteflowChainId
import com.neo.caption.ocr.module.liteflow.LiteflowService
import com.neo.caption.ocr.module.ocr.OcrProfileTable
import com.neo.caption.ocr.module.project.CaptionRowTable
import com.neo.caption.ocr.module.project.ProjectMetadataTable
import com.neo.caption.ocr.module.project.ProjectTable
import com.neo.caption.ocr.service.LoaderService
import org.bytedeco.opencv.opencv_java
import org.jetbrains.exposed.sql.SchemaUtils
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

@Component
@Transactional
class AppInitialize(
    private val loaderService: LoaderService,
    private val appService: AppService,
    private val commonProperties: CommonProperties,
    private val nodeService: LiteflowService,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        loaderService.loadLib(opencv_java::class.java)
        val system = loaderService.systemInformation()
        logging.debug { "System: [$system]" }
        logging.debug { "JavaCPP: [${loaderService.javacpp()}]" }
        logging.info {
            """
                |Caption OCR Tool service (v${appService.getAppInfoDto().version}) is ready!
                |Open http://${system.ip}:${system.port} in your browser.
            """.trimMargin("|")
        }
        commonProperties.workingDirectory.takeUnless { it.exists() }?.apply { this.createDirectory() }
        createTable()
        loadChain()
    }

    @Async
    fun createTable() {
        SchemaUtils.create(
            AppConfigTable,
            AstModelTable,
            AstEntityTable,
            AstEdgeTable,
            OcrProfileTable,
            ProjectTable,
            ProjectMetadataTable,
            CaptionRowTable
        )
    }

    @Async
    fun loadChain() {
        nodeService.createChain(
            chainId = LiteflowChainId(COCR_PROJECT_CHAIN),
            el = nodeService.buildProjectEl()
        )
        nodeService.createChain(
            chainId = LiteflowChainId(COCR_BATCH_OCR_CHAIN),
            el = nodeService.buildBatchOcrEl()
        )
    }

}