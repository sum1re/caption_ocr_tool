package com.neo.caption.ocr.module.liteflow

import com.fasterxml.jackson.annotation.JsonInclude
import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.support.convert
import org.jetbrains.exposed.dao.id.UUIDTable
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

object AstModelTable : UUIDTable("AST_MODEL") {
    val friendlyName = varchar("FRIENDLY_NAME", 255).uniqueIndex()
    val createdAt = long("CREATED_AT").clientDefault { Instant.now().epochSecond }
}

object AstEntityTable : UUIDTable("AST_ENTITY") {
    val astModelId = uuid("AST_MODEL_ID")
    val name = varchar("BEAN_NAME", 255)
    val label = varchar("ENTITY_LABEL", 255)
    val entityType = customEnumeration(
        name = "ENTITY_TYPE",
        sql = "ENUM(${EntityTypeEnum.entries.joinToString(",") { "'$it'" }})",
        fromDb = { EntityTypeEnum.valueOf(it as String) },
        toDb = { it.name }
    )
    val x = integer("GRAPH_X").default(0)
    val y = integer("GRAPH_Y").default(0)
    val data = text("NODE_DATA").nullable()
}

object AstEdgeTable : UUIDTable("AST_EDGE") {
    val astModelId = uuid("AST_MODEL_ID")
    val sourceId = uuid("SOURCE_ENTITY_ID")
    val targetId = uuid("TARGET_ENTITY_ID")
    val ifFlag = bool("IF_FLAG").nullable()
    val switchTag = varchar("SWITCH_TAG", 255).nullable()
}

data class AstModel(
    val id: UUID = UUID(0, 0),
    val friendlyName: String,
    val entityList: List<AstEntity>,
    val edgeList: List<AstEdge>,
) : BaseData

data class AstEntity(
    val id: UUID,
    val name: String,
    val label: String,
    val entityType: EntityTypeEnum,
    val x: Int,
    val y: Int,
    val data: String?,
) : BaseData

data class AstEdge(
    val source: UUID,
    val target: UUID,
    val ifFlag: Boolean? = null,
    val switchTag: String? = null,
) : BaseData

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AstModelDto(
    val id: UUID,
    val friendlyName: String,
    val entityList: List<AstEntityDto>,
    val edgeList: List<AstEdgeDto>
) : BaseDto

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AstEntityDto(
    val id: UUID,
    val name: String,
    val label: String,
    val entityType: EntityTypeEnum,
    val x: Int,
    val y: Int,
    val data: String?,
) : BaseDto

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AstEdgeDto(
    val source: UUID,
    val target: UUID,
    val ifFlag: Boolean?,
    val switchTag: String?,
) : BaseDto

@Component
class AstModelToNodeConverter : Converter<AstModel, BaseEntity> {
    override fun convert(source: AstModel): BaseEntity {
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
            when (entity.entityType) {
                EntityTypeEnum.SWITCH -> {
                    source.edgeList.filter { entity.id == it.source && it.switchTag.isNullOrBlank() }
                        .run { require(this.isEmpty()) { "Invalid Syntax Tree: ${entity.label} don't set tag for each deg" } }
                    validBranchCount++
                }

                EntityTypeEnum.IF -> {
                    val edges = source.edgeList.filter { entity.id == it.source && it.ifFlag != null }
                    require(edges.size == 2 && edges.first().ifFlag != edges.last().ifFlag) {
                        "Invalid Syntax Tree: ${entity.label} should both have two deg for true and false"
                    }
                    validBranchCount++
                }

                EntityTypeEnum.PARALLEL -> validBranchCount++
                EntityTypeEnum.START -> startNodeCount++
                EntityTypeEnum.END -> endNodeCount++
                EntityTypeEnum.SUMMARY -> summaryNodeCount++
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
            it.id to when (it.entityType) {
                EntityTypeEnum.COMMON -> CommonEntity(it.id, it.name, data = it.data)
                EntityTypeEnum.PARALLEL -> ParallelEntity(it.id, it.name)
                EntityTypeEnum.IF -> IfEntity(it.id, it.name, data = it.data)
                EntityTypeEnum.SWITCH -> SwitchEntity(it.id, it.name, data = it.data)
                EntityTypeEnum.SUMMARY -> SummaryEntity(it.id, it.name)
                EntityTypeEnum.START -> StartEntity(it.id, it.name)
                EntityTypeEnum.END -> EndEntity(it.id, it.name)
            }
        }
        source.edgeList.forEach {
            val sourceNode = nodeMap[it.source]!! // already contract
            val targetNode = nodeMap[it.target]!! // already contract
            when (sourceNode.type) {
                EntityTypeEnum.IF -> {
                    sourceNode as IfEntity
                    if (it.ifFlag!!) // already contract
                        sourceNode.trueNode = targetNode
                    else
                        sourceNode.falseNode = targetNode
                }

                EntityTypeEnum.SWITCH -> {
                    sourceNode as SwitchEntity
                    sourceNode.tagMap[targetNode] = it.switchTag!! // already contract
                }

                else -> {} // nothing to do
            }
            sourceNode.addNextNode(targetNode)
            targetNode.addPreviousNode(sourceNode)
        }
        nodeMap.values.forEach {
            when (it.type) {
                EntityTypeEnum.COMMON -> require(it.positiveNodeList.size == 1 && it.negativeNodeList.size == 1) {
                    "Invalid Syntax Tree: common entity must have one positive deg and one negative deg"
                }

                EntityTypeEnum.START -> require(it.positiveNodeList.size == 0 && it.negativeNodeList.size == 1) {
                    "Invalid Syntax Tree: start entity must have one negative deg and none positive deg"
                }

                EntityTypeEnum.END -> require(it.positiveNodeList.size == 1 && it.negativeNodeList.size == 0) {
                    "Invalid Syntax Tree: end entity must have one positive deg and none negative deg"
                }

                else -> {} // nothing to do
            }
        }
        return nodeMap.values.first()
    }
}

// All converter written by Gemini

@Component
class AstModelToAstModelDtoConverter : Converter<AstModel, AstModelDto> {
    override fun convert(source: AstModel): AstModelDto = AstModelDto(
        id = source.id,
        friendlyName = source.friendlyName,
        entityList = source.entityList.map { it.convert<AstEntityDto>() },
        edgeList = source.edgeList.map { it.convert<AstEdgeDto>() }
    )
}

@Component
class AstModelDtoToAstModelConverter : Converter<AstModelDto, AstModel> {
    override fun convert(source: AstModelDto): AstModel = AstModel(
        id = source.id,
        friendlyName = source.friendlyName,
        entityList = source.entityList.map { it.convert<AstEntity>() },
        edgeList = source.edgeList.map { it.convert<AstEdge>() }
    )
}

@Component
class AstEntityToAstEntityDtoConverter : Converter<AstEntity, AstEntityDto> {
    override fun convert(source: AstEntity): AstEntityDto = AstEntityDto(
        id = source.id,
        name = source.name,
        label = source.label,
        entityType = source.entityType,
        x = source.x,
        y = source.y,
        data = source.data
    )
}

@Component
class AstEntityDtoToAstEntityConverter : Converter<AstEntityDto, AstEntity> {
    override fun convert(source: AstEntityDto): AstEntity = AstEntity(
        id = source.id,
        name = source.name,
        label = source.label,
        entityType = source.entityType,
        x = source.x,
        y = source.y,
        data = source.data
    )
}

@Component
class AstEdgeToAstEdgeDtoConverter : Converter<AstEdge, AstEdgeDto> {
    override fun convert(source: AstEdge): AstEdgeDto = AstEdgeDto(
        source = source.source,
        target = source.target,
        ifFlag = source.ifFlag,
        switchTag = source.switchTag
    )
}

@Component
class AstEdgeDtoToAstEdgeConverter : Converter<AstEdgeDto, AstEdge> {
    override fun convert(source: AstEdgeDto): AstEdge = AstEdge(
        source = source.source,
        target = source.target,
        ifFlag = source.ifFlag,
        switchTag = source.switchTag
    )
}
