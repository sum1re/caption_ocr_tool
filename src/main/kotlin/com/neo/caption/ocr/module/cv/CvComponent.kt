package com.neo.caption.ocr.module.cv

import com.yomahub.liteflow.annotation.LiteflowComponent
import com.yomahub.liteflow.core.NodeComponent
import org.opencv.core.Mat
import kotlin.reflect.KClass

data class CvContext(
    val originMat: Mat, // initial by flow executor
    var nextMat: Mat = originMat.clone(), // consume by each NodeComponent,
    val matHistory: MutableList<Mat> = mutableListOf(originMat.clone()) // store history
) {
    fun produce(action: Mat.() -> Any) {
        when (val result = action(nextMat.clone())) {
            is Mat -> nextMat = result // action returns Mat, just set nextMat to it
            is Unit -> {} // action returns Unit, meaning that the operation occurs directly on nextMat without manipulation.
        }
        matHistory.add(nextMat.clone())
    }

    fun release() {
        originMat.release()
        nextMat.release()
        matHistory.forEach { it.release() }
        matHistory.clear()
    }
}

abstract class CvComponent<T : Any, R : Any> : NodeComponent() {
    abstract val kClass: KClass<T>
    lateinit var componentParam: T
    abstract val action: Mat.() -> R

    override fun isAccess(): Boolean {
        componentParam = this.getCmpData(kClass.java) ?: return false
        return true
    }

    override fun process() {
        context().produce(action)
    }

    override fun afterProcess() {
        super.afterProcess()
        context().release()
    }
}

@LiteflowComponent("crop")
class CropComponent : CvComponent<CropRange, Mat>() {
    override val kClass = CropRange::class
    override val action: Mat.() -> Mat = { this.crop(componentParam) }
}

@LiteflowComponent("morphology")
class MorphologyComponent : CvComponent<Morphology, Unit>() {
    override val kClass = Morphology::class
    override val action: Mat.() -> Unit = { this.morphology(componentParam) }
}

private fun NodeComponent.context(): CvContext = this.getContextBean(CvContext::class.java)
