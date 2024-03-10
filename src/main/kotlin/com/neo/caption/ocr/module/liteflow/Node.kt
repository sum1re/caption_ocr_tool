package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.domain.BaseData

enum class NodeTypeEnum {
    COMMON, WHEN, IF, SWITCH, SUMMARY, START, END
}

sealed interface Node : BaseData {
    val id: String
    val name: String
    val type: NodeTypeEnum
    val previousList: MutableList<Node>
    val nextList: MutableList<Node>

    fun addPreviousNode(node: Node) = previousList.add(node)
    fun addNextNode(node: Node) = nextList.add(node)
}

data class EntityAxis(val x: Int, val y: Int)

data class NodeEntity(
    val id: String, // unique id
    val name: String, // bean name
    val label: String,
    val nodeType: NodeTypeEnum,
    val axis: EntityAxis
) {
    init {
        require(id.isNotBlank()) { "missing node id" }
        require(name.isNotBlank()) { "missing node name" }
        require(label.isNotBlank()) { "missing node label" }
    }
}

data class NodeEdge(
    val source: String, // source entity id
    val target: String, // target entity id
    val isIfNode: Boolean = false, // only for IfNode
    val switchTag: String // only for SwitchNode
) {
    init {
        require(source.isNotBlank() && target.isNotBlank()) { "missing node edge" }
    }
}

data class AstModel(
    val nodeEntityList: List<NodeEntity>,
    val nodeEdgeList: List<NodeEdge>,
) : BaseData

data class StartNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.START,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf()
) : Node

data class EndNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.END,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf()
) : Node

data class CommonNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.COMMON,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf()
) : Node

data class WhenNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.WHEN,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf()
) : Node

data class SwitchNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.SWITCH,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf(),
    val tagMap: MutableMap<Node, String> = mutableMapOf()
) : Node {
    fun putTag(node: Node, tag: String) {
        tagMap[node] = tag
        addNextNode(node)
    }
}

data class IfNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.IF,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf(),
    val trueNode: Node?,
    val falseNode: Node?
) : Node {
    init {
        if (trueNode != null) addNextNode(trueNode)
        if (falseNode != null) addNextNode(falseNode)
    }
}

data class SummaryNode(
    override val id: String,
    override val name: String,
    override val type: NodeTypeEnum = NodeTypeEnum.SUMMARY,
    override val previousList: MutableList<Node> = mutableListOf(),
    override val nextList: MutableList<Node> = mutableListOf(),
) : Node
