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

data class EntityAxis(val x: Int, val y: Int) : BaseData

data class Entity(
    val id: String, // must be a unique id or start or end
    val name: String, // must be bean name or start or end
    val label: String = "", // used in frontend, empty is safe
    val nodeType: NodeTypeEnum,
    val axis: EntityAxis = EntityAxis(0, 0), // used in frontend, (0,0) as default
    val data: String = "" // used in flow component
) : BaseData {
    init {
        require(id.isNotBlank()) { "missing entity id" }
        require(name.isNotBlank()) { "missing entity name" }
    }
}

data class Edge(
    val source: String, // source entity id or start or end
    val target: String, // target entity id or start or end
    val ifFlag: Boolean?, // only for IfNode
    val switchTag: String? // only for SwitchNode
) : BaseData {
    init {
        require(source.isNotBlank()) { "edge require a source entity" }
        require(target.isNotBlank()) { "edge require a target entity" }
    }
}

data class AstModel(
    val entityList: List<Entity>,
    val edgeList: List<Edge>,
) : BaseData

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
class AstModelToNodeConverter : Converter<AstModel, BaseNode> {
    override fun convert(source: AstModel): BaseNode {
        // check AstModel
        require(source.entityList.isNotEmpty()) { "The node cannot be empty" }
        source.entityList.map { it.id }.toSortedSet().run {
            source.edgeList.map { listOf(it.source, it.target) }.flatten().toSortedSet().let {
                require(this == it) { "Missing the following node(s) id: ${(this - it) + (it - this)}" }
            }
        }
        var validBranchCount = 0
        var startNodeCount = 0
        var endNodeCount = 0
        var summaryNodeCount = 0
        source.entityList.forEach { entity ->
            when (entity.nodeType) {
                NodeTypeEnum.SWITCH -> {
                    source.edgeList.filter { entity.id == it.source && it.switchTag.isNullOrBlank() }
                        .run { require(this.isEmpty()) { "Invalid Syntax Tree: ${entity.label} don't set tag for each deg" } }
                    validBranchCount++
                }

                NodeTypeEnum.IF -> {
                    val edges = source.edgeList.filter { entity.id == it.source && it.ifFlag != null }
                    require(edges.size == 2 && edges.first().ifFlag != edges.last().ifFlag) {
                        "Invalid Syntax Tree: ${entity.label} should both have two deg for true and false"
                    }
                    validBranchCount++
                }

                NodeTypeEnum.WHEN -> validBranchCount++
                NodeTypeEnum.START -> startNodeCount++
                NodeTypeEnum.END -> endNodeCount++
                NodeTypeEnum.SUMMARY -> summaryNodeCount++
                else -> {} //nothing to do
            }
        }
        require(summaryNodeCount == validBranchCount) {
            "Invalid Syntax Tree: IF, SWITCH, WHEN entities must have SUMMARY entity to summarize"
        }
        require(startNodeCount == 1) { "Invalid Syntax Tree: ast must have one unique start node" }
        require(endNodeCount == 1) { "Invalid Syntax Tree: ast must have one unique end node" }
        // converter
        val nodeMap = source.entityList.associate {
            it.id to when (it.nodeType) {
                NodeTypeEnum.COMMON -> CommonNode(it.id, it.name, data = it.data)
                NodeTypeEnum.WHEN -> WhenNode(it.id, it.name)
                NodeTypeEnum.IF -> IfNode(it.id, it.name, data = it.data)
                NodeTypeEnum.SWITCH -> SwitchNode(it.id, it.name, data = it.data)
                NodeTypeEnum.SUMMARY -> SummaryNode(it.id, it.name)
                NodeTypeEnum.START -> StartNode(it.id, it.name)
                NodeTypeEnum.END -> EndNode(it.id, it.name)
            }
        }
        source.edgeList.forEach {
            val sourceNode = nodeMap[it.source]!! // already check above
            val targetNode = nodeMap[it.target]!! // already check above
            when (sourceNode.type) {
                NodeTypeEnum.IF -> {
                    sourceNode as IfNode
                    if (it.ifFlag!!)
                        sourceNode.trueNode = targetNode
                    else
                        sourceNode.falseNode = targetNode
                }

                NodeTypeEnum.SWITCH -> (sourceNode as SwitchNode).tagMap[targetNode] = it.switchTag!!
                else -> {} // nothing to do
            }
            sourceNode.addNextNode(targetNode)
            targetNode.addPreviousNode(sourceNode)
        }
        nodeMap.values.forEach {
            when (it.type) {
                NodeTypeEnum.COMMON -> require(it.positiveNodeList.size == 1 && it.negativeNodeList.size == 1) {
                    "Invalid Syntax Tree: common entity must have one positive deg and one negative deg"
                }

                NodeTypeEnum.START -> require(it.positiveNodeList.size == 0 && it.negativeNodeList.size == 1) {
                    "Invalid Syntax Tree: start entity must have one negative deg and none positive deg"
                }

                NodeTypeEnum.END -> require(it.positiveNodeList.size == 1 && it.negativeNodeList.size == 0) {
                    "Invalid Syntax Tree: end entity must have one positive deg and none negative deg"
                }

                else -> {} // nothing to do
            }
        }
        return nodeMap.values.first()
    }
}

@Component
class NodeToElConverter : Converter<BaseNode, String> {
    override fun convert(source: BaseNode): String {
        val stack = mutableListOf<ThenELWrapper>()
        val summarySet = mutableSetOf<String>()
        return source.toEl(ThenELWrapper(), stack, summarySet).toEL()
    }

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
                stack.push(wrapper)
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
                stack.push(wrapper)
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
                stack.push(wrapper)
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
                    it.toEl(stack.pop(), stack, doneSummary)
                }
            }

            NodeTypeEnum.START -> this.negativeNodeList.forEach { it.toEl(wrapper, stack, doneSummary) }
            NodeTypeEnum.END -> {} // nothing to do
        }
        return wrapper
    }

    private fun <T> MutableList<T>.push(t: T) = this.add(0, t)

    private fun <T> MutableList<T>.pop() = this.removeFirst()

    private fun <T : BaseNode> T.wrap() = ELBus.node(this.name).also {
        if (this is BaseNodeWithData && this.data.isNotBlank()) it.data(randomDataName(this.name), this.data)
    }

    private fun randomDataName(prefix: String): String = "$prefix-${UUID.randomUUID().toString().substring(0, 8)}"

}
