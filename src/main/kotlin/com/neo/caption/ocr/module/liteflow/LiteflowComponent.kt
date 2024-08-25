package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.MAT_EXTENSION
import com.neo.caption.ocr.common.throwLiteFlowException
import com.neo.caption.ocr.module.app.CompareAlgorithm
import com.neo.caption.ocr.module.app.MatRetentionPolicy
import com.neo.caption.ocr.module.cv.AdaptiveBinarization
import com.neo.caption.ocr.module.cv.BilateralFilter
import com.neo.caption.ocr.module.cv.BoxFilter
import com.neo.caption.ocr.module.cv.CropRange
import com.neo.caption.ocr.module.cv.FixedBinarization
import com.neo.caption.ocr.module.cv.GaussianBlur
import com.neo.caption.ocr.module.cv.HLSRange
import com.neo.caption.ocr.module.cv.HSVRange
import com.neo.caption.ocr.module.cv.Morphology
import com.neo.caption.ocr.module.cv.SingleIntParam
import com.neo.caption.ocr.module.cv.adaptiveBinarization
import com.neo.caption.ocr.module.cv.bilateralFilter
import com.neo.caption.ocr.module.cv.boxFilter
import com.neo.caption.ocr.module.cv.calcBlackPixel
import com.neo.caption.ocr.module.cv.calcPSNR
import com.neo.caption.ocr.module.cv.calcSSIM
import com.neo.caption.ocr.module.cv.crop
import com.neo.caption.ocr.module.cv.cvtColor
import com.neo.caption.ocr.module.cv.cvtType
import com.neo.caption.ocr.module.cv.equalizeHist
import com.neo.caption.ocr.module.cv.fixedBinarization
import com.neo.caption.ocr.module.cv.gaussianBlur
import com.neo.caption.ocr.module.cv.inRange
import com.neo.caption.ocr.module.cv.medianBlur
import com.neo.caption.ocr.module.cv.morphology
import com.neo.caption.ocr.module.cv.toByteArray
import com.neo.caption.ocr.module.file.FileService
import com.neo.caption.ocr.module.ocr.OcrSpaceConfig
import com.neo.caption.ocr.module.ocr.TesseractConfig
import com.neo.caption.ocr.module.project.CaptionRow
import com.neo.caption.ocr.module.project.ProjectService
import com.neo.caption.ocr.service.LoaderService
import com.neo.caption.ocr.support.convert
import com.neo.caption.ocr.support.findMaxBy
import com.neo.caption.ocr.support.findMidBy
import com.neo.caption.ocr.support.findMinBy
import com.yomahub.liteflow.annotation.LiteflowComponent
import com.yomahub.liteflow.builder.el.ThenELWrapper
import com.yomahub.liteflow.core.NodeBooleanComponent
import com.yomahub.liteflow.core.NodeComponent
import com.yomahub.liteflow.core.NodeIteratorComponent
import com.yomahub.liteflow.core.NodeSwitchComponent
import com.yomahub.liteflow.flow.LiteflowResponse
import org.bytedeco.tesseract.TessBaseAPI
import org.opencv.core.Mat
import org.opencv.videoio.Videoio
import java.math.BigDecimal
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.walk
import kotlin.reflect.KClass

/**
 * NodeComponent class convention:
 *
 * Name of NodeComponent should start with Liteflow
 * followed by the component id.
 *
 *  ```
 *  @LiteflowComponent("actionA")
 *  class LiteflowActionA : NodeComponent()
 *
 *  @LiteflowComponent("switchEnum")
 *  class LiteflowSwitchEnum : NodeSwitchComponent()
 *  ```
 */

//=========== BreakComponent =================
@LiteflowComponent("exitLoop")
class LiteflowExitLoop : NodeBooleanComponent() {
    override fun processBoolean() = true
    override fun isEnd(): Boolean = true

}

//=========== CvComponent ====================
abstract class CvComponent<T : Any, R : Any> : NodeComponent() {
    abstract val kClass: KClass<T>
    lateinit var componentParam: T
    abstract val action: Mat.() -> R

    override fun isAccess(): Boolean {
        componentParam = this.getCmpData(kClass.java) ?: return false
        return true
    }

    override fun process() {
        context<FilterContext>().produce(action)
    }
}

@LiteflowComponent("crop")
class LiteflowCrop : CvComponent<CropRange, Mat>() {
    override val kClass = CropRange::class
    override val action: Mat.() -> Mat = { this.crop(componentParam) }
}

@LiteflowComponent("morphology")
class LiteflowMorphology : CvComponent<Morphology, Unit>() {
    override val kClass = Morphology::class
    override val action: Mat.() -> Unit = { this.morphology(componentParam) }
}

@LiteflowComponent("adaptiveBinarization")
class LiteflowAdaptiveBinarization : CvComponent<AdaptiveBinarization, Unit>() {
    override val kClass = AdaptiveBinarization::class
    override val action: Mat.() -> Unit = { this.adaptiveBinarization(componentParam) }
}

@LiteflowComponent("fixedBinarization")
class LiteflowFixedBinarization : CvComponent<FixedBinarization, Unit>() {
    override val kClass = FixedBinarization::class
    override val action: Mat.() -> Unit = { this.fixedBinarization(componentParam) }
}

@LiteflowComponent("bilateralFilter")
class LiteflowBilateralFilter : CvComponent<BilateralFilter, Unit>() {
    override val kClass = BilateralFilter::class
    override val action: Mat.() -> Unit = { this.bilateralFilter(componentParam) }
}

@LiteflowComponent("boxFilter")
class LiteflowBoxFilter : CvComponent<BoxFilter, Unit>() {
    override val kClass = BoxFilter::class
    override val action: Mat.() -> Unit = { this.boxFilter(componentParam) }
}

@LiteflowComponent("gaussianBlur")
class LiteflowGaussianBlur : CvComponent<GaussianBlur, Unit>() {
    override val kClass = GaussianBlur::class
    override val action: Mat.() -> Unit = { this.gaussianBlur(componentParam) }
}

@LiteflowComponent("medianBlur")
class LiteflowMedianBlur : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.medianBlur(componentParam) }
}

@LiteflowComponent("hlsRange")
class LiteflowHLSRange : CvComponent<HLSRange, Unit>() {
    override val kClass = HLSRange::class
    override val action: Mat.() -> Unit = {
        this.inRange(
            componentParam.hueMin,
            componentParam.lightnessMin,
            componentParam.saturationMin,
            componentParam.hueMax,
            componentParam.lightnessMax,
            componentParam.saturationMax
        )
    }
}

@LiteflowComponent("hsvRange")
class LiteflowHSVRange : CvComponent<HSVRange, Unit>() {
    override val kClass = HSVRange::class
    override val action: Mat.() -> Unit = {
        this.inRange(
            componentParam.hueMin,
            componentParam.saturationMin,
            componentParam.valueMin,
            componentParam.hueMax,
            componentParam.saturationMax,
            componentParam.valueMax
        )
    }
}

@LiteflowComponent("convertColor")
class LiteflowConvertColor : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.cvtColor(componentParam) }
}

@LiteflowComponent("convertDepth")
class LiteflowConvertDepth : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.cvtType(componentParam) }
}

@LiteflowComponent("equalize")
class LiteflowEqualize : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.equalizeHist(componentParam) }
}

// TODO: add flow for arithmetic operation

//=========== WhileComponent =================
@LiteflowComponent("videoGrab")
class LiteflowVideoGrab : NodeBooleanComponent() {
    override fun processBoolean(): Boolean {
        return context<ProjectContext>().run {
            this.videoCaption.grab().also { if (!it) this.isFinish.set(true) }
        }
    }
}

//=========== IteratorComponent ==============
@LiteflowComponent("findMatInPath")
class LiteflowFindMatInPath(
    private val projectService: ProjectService
) : NodeIteratorComponent() {
    @OptIn(ExperimentalPathApi::class)
    override fun processIterator(): Iterator<Path> =
        context<BatchOcrContext>().let { projectService.generateWorkingDirectory(it.projectId) }
            .walk(PathWalkOption.BREADTH_FIRST)
            .filter { !it.isDirectory() && it.extension == MAT_EXTENSION }
            .sorted()
            .iterator()
}

//=========== SwitchComponent ================
@LiteflowComponent("switchCompareAlgorithm")
class LiteflowSwitchCompareAlgorithm : NodeSwitchComponent() {
    override fun processSwitch(): String = "tag:${context<ProjectContext>().appConfig.compareAlgorithm}"
    override fun isAccess(): Boolean = !context<ProjectContext>().templateMat.empty()
}

@LiteflowComponent("switchOcr")
class LiteflowSwitchOcr : NodeSwitchComponent() {
    override fun processSwitch(): String = "tag:${context<ProjectContext>().project.ocrProfile?.ocrType}"
}

//=========== IfComponent ====================
@LiteflowComponent("isInFilterInterval")
class LiteflowIsInCheckFilterInterval : NodeBooleanComponent() {
    override fun processBoolean(): Boolean =
        context<ProjectContext>().let { it.count.get() % it.appConfig.filterInterval != 0 }
}

@LiteflowComponent("isMatSingleChannel")
class LiteflowIsMatSingleChannel : NodeBooleanComponent() {
    override fun processBoolean(): Boolean = context<ProjectContext>().matStack.last().channels() == 1
}

@LiteflowComponent("meetPixelThreshold")
class LiteflowMeetPixelThreshold : NodeBooleanComponent() {
    override fun processBoolean(): Boolean {
        val context = context<ProjectContext>()
        context.pixelCount.set(context.matStack.last().calcBlackPixel())
        return context.pixelCount.get() >= context<ProjectContext>().appConfig.pixelThreshold
    }
}

@LiteflowComponent("needOcrProcessing")
class LiteflowNeedOcrProcessing : NodeBooleanComponent() {
    override fun processBoolean(): Boolean = context<ProjectContext>().project.ocrProfile != null
}

@LiteflowComponent("canProceed")
class LiteflowCanProceed : NodeBooleanComponent() {
    override fun processBoolean(): Boolean {
        context<ProjectContext>().similarGroup.clear()
        return !context<ProjectContext>().isFinish.get()
    }
}

@LiteflowComponent("compareThreshold")
class LiteflowCompareThreshold : NodeBooleanComponent() {
    override fun processBoolean(): Boolean = context<ProjectContext>().similar.get()
        .let { it.second == context<ProjectContext>().appConfig.similarThreshold[it.first] }
}

@LiteflowComponent("hasTemplate")
class LiteflowHasTemplate : NodeBooleanComponent() {
    override fun processBoolean(): Boolean = !context<ProjectContext>().templateMat.empty()
}

@LiteflowComponent("isStackEmpty")
class LiteflowIsStackEmpty : NodeBooleanComponent() {
    override fun processBoolean(): Boolean = context<ProjectContext>().matStack.isEmpty()
}

//=========== CommonComponent ================
@LiteflowComponent("assignTemplate")
class LiteflowAssignTemplate : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        context.matStack.last().copyTo(context.templateMat)
    }
}

@LiteflowComponent("startProject")
class LiteflowStartProject(
    private val projectService: ProjectService,
    private val liteflowService: LiteflowService

) : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        val file = projectService.generateVideoPath(context.projectId, context.project.projectMetadata.extension)
        require(file.exists()) { throwLiteFlowException(ErrorCodeEnum.VIDEO_READ_ERROR) }
        context.videoCaption.open(file.absolutePathString())
        require(context.videoCaption.isOpened) { throwLiteFlowException(ErrorCodeEnum.VIDEO_READ_ERROR) }
        context.videoCaption.set(Videoio.CAP_PROP_POS_FRAMES, 0.0)
    }

    override fun isAccess(): Boolean {
        val context = context<ProjectContext>()
        val el = context.project.astModel
            .convert<BaseEntity>()
            .convert<ThenELWrapper>()
        liteflowService.createChain(LiteflowChainId(context.projectId), el)
        return true
    }
}

@LiteflowComponent("startBatchOcr")
class LiteflowStartBatchOcr : NodeComponent() {
    override fun process() {} // nothing to do

    override fun isAccess(): Boolean {
        context<BatchOcrContext>()
        return true
    }
}

@LiteflowComponent("finishProject")
class LiteflowFinishProject(
    private val liteflowService: LiteflowService,
) : NodeComponent() {
    override fun process() {
        liteflowService.removeChain(LiteflowChainId(context<ProjectContext>().projectId))
    }
}

@LiteflowComponent("retrieveVideoMat")
class LiteflowRetrieveVideoMat : NodeComponent() {
    override fun process() {
        val projectContext = context<ProjectContext>()
        projectContext.releaseMatStack()
        val mat = Mat()
        projectContext.videoCaption.retrieve(mat)
        projectContext.matStack.add(mat)
    }
}

@LiteflowComponent("mergeGroup")
class LiteflowMergeGroup : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        val startIndex = context.similarGroup.first().start
        val endIndex = context.similarGroup.last().start
        val mat: Mat = if (context.similarGroup.isEmpty()) Mat()
        else when (context<ProjectContext>().appConfig.matRetentionPolicy) {
            MatRetentionPolicy.FIRST_IN -> context.similarGroup.first().mat.clone()
            MatRetentionPolicy.LAST_IN -> context.similarGroup.last().mat.clone()
            MatRetentionPolicy.WHITEST -> context.similarGroup.findMinBy { it.pixel }!!.mat.clone()
            MatRetentionPolicy.BLACKEST -> context.similarGroup.findMaxBy { it.pixel }!!.mat.clone()
            MatRetentionPolicy.BALANCED -> context.similarGroup.findMidBy { it.pixel }!!.mat.clone()
        }
        context.similarGroup.clear()
        context.similarGroup.add(
            RowMetadata(
                start = startIndex,
                end = endIndex,
                mat = mat
            )
        )
    }
}

@LiteflowComponent("saveMat")
class LiteflowSaveMat(
    private val fileService: FileService,
    private val projectService: ProjectService
) : NodeComponent() {
    override fun process() {
        val projectContext = context<ProjectContext>()
        val rowMetadata = projectContext.similarGroup.first()
        val captionRow = CaptionRow(
            projectId = context<ProjectContext>().projectId,
            start = BigDecimal(rowMetadata.start.toString()),
            end = BigDecimal(rowMetadata.end.toString()),
        )
        val captionId = projectService.insertCaptionRow(captionRow)
        fileService.saveMat(
            savedPath = projectService.generateMatPath(captionRow.projectId, captionId),
            mat = rowMetadata.mat
        )
        rowMetadata.id.set(captionId)
    }

    override fun isAccess(): Boolean = context<ProjectContext>().similarGroup.isNotEmpty()
}

@LiteflowComponent("ssimAlgorithm")
class LiteflowSsimAlgorithm : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        context.similar.set(Pair(CompareAlgorithm.SSIM, context.templateMat.calcSSIM(context.matStack.last())))
    }
}

@LiteflowComponent("psnrAlgorithm")
class LiteflowPsnrAlgorithm : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        context.similar.set(Pair(CompareAlgorithm.PSNR, context.templateMat.calcPSNR(context.matStack.last())))
    }
}

@LiteflowComponent("pushGroup")
class LiteflowPushGroup : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        val rowMetadata = RowMetadata(
            start = context<ProjectContext>().count.get(),
            pixel = context.pixelCount.get(),
            mat = context.matStack.last().clone()
        )
        context.similarGroup.add(rowMetadata)
    }
}

@LiteflowComponent("publishProgress")
class LiteflowPublishProgress : NodeComponent() {
    override fun process() {
        context<ProjectContext>().count.incrementAndGet()
        TODO("publish count")
    }
}

@LiteflowComponent("ocrTesseract")
class LiteflowOcrTesseract(
    private val loaderService: LoaderService,
    private val projectService: ProjectService,
) : NodeComponent() {
    override fun process() {
        val rowMetadata = context<ProjectContext>().similarGroup.first()
        val api = context<OcrContext>().api.get() as TessBaseAPI
        rowMetadata.mat.also { api.SetImage(it.toByteArray(), it.cols(), it.rows(), it.channels(), it.cols()) }
        val caption = api.GetUTF8Text().use { if (it.isNull) "" else it.string }
        projectService.updateCaption(rowMetadata.id.get(), caption)
    }

    override fun isAccess(): Boolean {
        val ocrContext = context<OcrContext>()
        if (ocrContext.api.get() != null) return true
        if (ocrContext.config.get() != null) return true
        val config = context<ProjectContext>().project.ocrProfile!!.config
        if (config !is TesseractConfig) return false
        ocrContext.config.set(config)
        ocrContext.api.set(TessBaseAPI().also {
            it.Init(
                loaderService.javacpp().tessdataDir,
                config.language,
                config.engine,
                ByteArray(0),
                config.vectorKey.size().toInt(),
                config.vectorKey,
                config.vectorValue,
                true
            )
        })
        ocrContext.releaseFun = {
            @Suppress("CAST_NEVER_SUCCEEDS")
            (this.api as TessBaseAPI).releaseReference()
        }
        return true
    }
}

@LiteflowComponent("ocrOcrSpace")
class LiteflowOcrOcrSpace : NodeComponent() {
    override fun process() {
        TODO()
    }

    override fun isAccess(): Boolean {
        val ocrContext = context<OcrContext>()
        if (ocrContext.api.get() != null) return true
        if (ocrContext.config.get() != null) return true
        val config = context<ProjectContext>().project.ocrProfile!!.config
        if (config !is OcrSpaceConfig) return false
        ocrContext.config.set(config)
        return true
    }
}

@LiteflowComponent("executeChain")
class LiteflowExecuteChain(
    private val liteflowService: LiteflowService
) : NodeComponent() {
    var result: LiteflowResponse? = null

    override fun process() {
        val projectContext = context<ProjectContext>()
        val outMat = Mat()
        val context = FilterContext(projectContext.matStack, outMat)
        result = liteflowService.executeSync(LiteflowChainId(projectContext.projectId), context)
    }

    override fun isEnd(): Boolean = if (result == null) true else !result!!.isSuccess
}

@LiteflowComponent("releaseResources")
class LiteflowReleaseResources : NodeComponent() {
    override fun process() {
        val projectContext = context<ProjectContext>()
        val ocrContext = context<OcrContext>()
        projectContext.videoCaption.release()
        projectContext.matStack.clear()
        projectContext.similarGroup.clear()
        projectContext.templateMat.release()
        if (projectContext.project.ocrProfile != null) {
            ocrContext.release()
        }
    }
}
