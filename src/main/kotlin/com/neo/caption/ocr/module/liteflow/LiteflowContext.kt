package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.module.app.AppConfig
import com.neo.caption.ocr.module.app.CompareAlgorithm
import com.neo.caption.ocr.module.ocr.OcrConfig
import com.neo.caption.ocr.module.project.Project
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

sealed interface BaseContext

/**
 * Provide global variables for the entire flow.
 */
data class ProjectContext(
    val project: Project,
    val projectId: UUID = project.id,
    val appConfig: AppConfig,
    val videoCaption: VideoCapture = VideoCapture(),
    val count: AtomicInteger = AtomicInteger(0),
    val isFinish: AtomicBoolean = AtomicBoolean(false),
    val pixelCount: AtomicInteger = AtomicInteger(0),
    val matStack: MutableList<Mat> = mutableListOf(),
    val templateMat: Mat = Mat(),
    val similar: AtomicReference<Pair<CompareAlgorithm, Number>> = AtomicReference(Pair(CompareAlgorithm.SSIM, 0.0)),
    val similarGroup: MutableList<RowMetadata> = mutableListOf()
) : BaseContext {
    fun releaseMatStack() {
        matStack.forEach { it.release() }
        matStack.clear()
    }
}

data class RowMetadata(
    val id: AtomicLong = AtomicLong(0),
    val start: Int,
    val end: Int = start,
    val pixel: Int = 0,
    val mat: Mat
)

/**
 * Provider variables for the filter flow which design by user.
 */
data class FilterContext(
    val matStack: MutableList<Mat>, // store history
    val resultMat: Mat
) : BaseContext {
    fun produce(action: Mat.() -> Any) {
        val copy = matStack.last().clone()!!
        when (val result = action(copy)) {
            is Mat -> matStack.add(result) // action returns Mat, add result to stack.
            is Unit -> matStack.add(copy) // action returns Unit, add copy to stack.
        }
    }
}

data class OcrContext(
    val config: AtomicReference<OcrConfig?> = AtomicReference(null),
    val api: AtomicReference<Any?> = AtomicReference(null),
    var releaseFun: (OcrContext.() -> Unit)? = null
) : BaseContext {
    fun release() {
        releaseFun?.let { this.it() }
    }
}

data class BatchOcrContext(
    val projectId: UUID,
    val project: Project,
    val count: AtomicInteger,
) : BaseContext
