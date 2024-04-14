package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.domain.BaseData
import com.yomahub.liteflow.builder.el.ELBus
import com.yomahub.liteflow.builder.el.ThenELWrapper
import com.yomahub.liteflow.builder.el.WhenELWrapper
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.UUID

enum class NodeTypeEnum {
    COMMON, WHEN, IF, SWITCH, SUMMARY, START, END
}

sealed interface BaseNode : BaseData {
    val id: String
    val name: String
    val type: NodeTypeEnum
    val positiveNodeList: MutableList<BaseNode>
    val negativeNodeList: MutableList<BaseNode>

    fun addPreviousNode(node: BaseNode) = positiveNodeList.add(node)
    fun addNextNode(node: BaseNode) = negativeNodeList.add(node)
}

sealed interface BaseNodeWithData : BaseNode {
    val data: String
}

data class StartNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.START,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf()
) : BaseNode

data class EndNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.END,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf()
) : BaseNode

data class CommonNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.COMMON,
    override val data: String,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf()
) : BaseNodeWithData

data class WhenNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.WHEN,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf()
) : BaseNode

data class SwitchNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.SWITCH,
    override val data: String,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf(),
    val tagMap: MutableMap<BaseNode, String> = mutableMapOf()
) : BaseNodeWithData

data class IfNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.IF,
    override val data: String,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf(),
    var trueNode: BaseNode? = null,
    var falseNode: BaseNode? = null
) : BaseNodeWithData

data class SummaryNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.SUMMARY,
    override val positiveNodeList: MutableList<BaseNode> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseNode> = mutableListOf(),
) : BaseNode

@Component
class NodeToElConverter : Converter<BaseNode, String> {
    override fun convert(source: BaseNode): String =
        source.toEl(ThenELWrapper(), mutableListOf(), mutableSetOf()).toEL()

    private fun BaseNode.toEl(
        wrapper: ThenELWrapper,
        stack: MutableList<ThenELWrapper>,
        doneSummary: MutableSet<String>
    ): ThenELWrapper {
        when (this.type) {
            NodeTypeEnum.COMMON -> {
                wrapper.then(this.wrap())
                this.negativeNodeList.forEach {
                    it.toEl(wrapper, stack, doneSummary)
                }
            }

            NodeTypeEnum.WHEN -> {
                stack.add(wrapper)
                val whenWrapper = WhenELWrapper().ignoreError(true)!!
                wrapper.then(whenWrapper)
                this.negativeNodeList.forEach { node ->
                    ThenELWrapper().also {
                        whenWrapper.`when`(it)
                        node.toEl(it, stack, doneSummary)
                    }
                }
            }

            NodeTypeEnum.IF -> {
                this as IfNode
                stack.add(wrapper)
                val trueElWrapper = ThenELWrapper()
                val falseElWrapper = ThenELWrapper()
                if (this.falseNode!!.type == NodeTypeEnum.SUMMARY) {
                    wrapper.then(ELBus.ifOpt(this.wrap(), trueElWrapper))
                } else {
                    wrapper.then(ELBus.ifOpt(this.wrap(), trueElWrapper, falseElWrapper))
                }
                this.trueNode!!.toEl(trueElWrapper, stack, doneSummary)
                this.falseNode!!.toEl(falseElWrapper, stack, doneSummary)
            }

            NodeTypeEnum.SWITCH -> {
                this as SwitchNode
                stack.add(wrapper)
                val switchWrapper = ELBus.switchOpt(this.wrap())!!
                wrapper.then(switchWrapper)
                this.tagMap.forEach {
                    val thenWrapper = ThenELWrapper().id(it.value)!!
                    switchWrapper.to(thenWrapper)
                    it.key.toEl(thenWrapper, stack, doneSummary)
                }
            }

            NodeTypeEnum.SUMMARY -> this.name.takeIf { it !in doneSummary }?.let { name ->
                doneSummary.add(name)
                this.negativeNodeList.forEach {
                    it.toEl(stack.removeLast(), stack, doneSummary)
                }
            }

            NodeTypeEnum.START -> this.negativeNodeList.forEach { it.toEl(wrapper, stack, doneSummary) }
            NodeTypeEnum.END -> {} // nothing to do
        }
        return wrapper
    }

    private fun <T : BaseNode> T.wrap() = ELBus.node(this.name).also {
        if (this is BaseNodeWithData && this.data.isNotBlank()) it.data(randomDataName(this.name), this.data)
    }

    private fun randomDataName(prefix: String): String = "$prefix-${UUID.randomUUID().toString().substring(0, 8)}"

}
