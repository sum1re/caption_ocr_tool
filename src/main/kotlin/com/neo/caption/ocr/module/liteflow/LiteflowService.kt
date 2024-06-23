package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.common.CommonService
import com.yomahub.liteflow.builder.el.ELWrapper
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder
import com.yomahub.liteflow.core.FlowExecutor
import com.yomahub.liteflow.flow.FlowBus
import com.yomahub.liteflow.flow.LiteflowResponse
import java.util.UUID
import java.util.concurrent.Future

data class LiteflowChainId(val id: String) {
    constructor(id: UUID) : this("chain-$id")
}

@CommonService
class LiteflowService(
    private val flowExecutor: FlowExecutor,
) {

    private val futureMap = mutableMapOf<UUID, Future<LiteflowResponse>>()

    fun validateEl(el: String): Boolean = LiteFlowChainELBuilder.validate(el)

    fun createChain(chainId: LiteflowChainId, el: ELWrapper) {
        el.toEL().takeIf { validateEl(it) }?.let {
            LiteFlowChainELBuilder.createChain().setChainId(chainId.id).setEL(it).build()
        } ?: throw RuntimeException("Invalid el")
    }

    fun removeChain(chainId: LiteflowChainId) {
        FlowBus.removeChain(chainId.id)
    }

    fun <T : BaseContext> executeAsync(taskId: UUID, chainId: LiteflowChainId, vararg context: T) {
        futureMap[taskId] = flowExecutor.execute2Future(chainId.id, null, *context)
    }

    fun <T : BaseContext> executeSync(chainId: LiteflowChainId, vararg contexts: T): LiteflowResponse =
        flowExecutor.execute2Resp(chainId.id, null, *contexts)

    fun getFuture(projectId: UUID): Future<LiteflowResponse>? = futureMap[projectId]

    fun cancel(projectId: UUID) {
        futureMap[projectId]?.cancel(true)
    }

    fun remove(projectId: UUID) {
        futureMap.remove(projectId)?.cancel(true)
    }

}