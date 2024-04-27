package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.domain.BaseData
import com.yomahub.liteflow.builder.el.ELBus
import com.yomahub.liteflow.builder.el.ThenELWrapper
import com.yomahub.liteflow.builder.el.WhenELWrapper
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.UUID

enum class EntityTypeEnum {
    COMMON, PARALLEL, IF, SWITCH, SUMMARY, START, END
}

sealed interface BaseEntity : BaseData {
    val id: UUID
    val name: String
    val type: EntityTypeEnum
    val positiveNodeList: MutableList<BaseEntity>
    val negativeNodeList: MutableList<BaseEntity>

    fun addPreviousNode(node: BaseEntity) = positiveNodeList.add(node)
    fun addNextNode(node: BaseEntity) = negativeNodeList.add(node)
}

sealed interface BaseEntityWithData : BaseEntity {
    val data: String?
}

data class StartEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.START,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf()
) : BaseEntity

data class EndEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.END,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf()
) : BaseEntity

data class CommonEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.COMMON,
    override val data: String?,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf()
) : BaseEntityWithData

data class ParallelEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.PARALLEL,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf()
) : BaseEntity

data class SwitchEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.SWITCH,
    override val data: String?,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf(),
    val tagMap: MutableMap<BaseEntity, String> = mutableMapOf()
) : BaseEntityWithData

data class IfEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.IF,
    override val data: String?,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf(),
    var trueNode: BaseEntity? = null,
    var falseNode: BaseEntity? = null
) : BaseEntityWithData

data class SummaryEntity(
    override val id: UUID,
    override val name: String,
    override val type: EntityTypeEnum = EntityTypeEnum.SUMMARY,
    override val positiveNodeList: MutableList<BaseEntity> = mutableListOf(),
    override val negativeNodeList: MutableList<BaseEntity> = mutableListOf(),
) : BaseEntity

@Component
class NodeToElConverter : Converter<BaseEntity, ThenELWrapper> {
    override fun convert(source: BaseEntity): ThenELWrapper =
        source.toEl(ThenELWrapper(), mutableListOf(), mutableSetOf())

    private fun BaseEntity.toEl(
        wrapper: ThenELWrapper,
        stack: MutableList<ThenELWrapper>,
        doneSummary: MutableSet<String>
    ): ThenELWrapper {
        when (this.type) {
            EntityTypeEnum.COMMON -> {
                wrapper.then(this.wrap())
                this.negativeNodeList.forEach {
                    it.toEl(wrapper, stack, doneSummary)
                }
            }

            EntityTypeEnum.PARALLEL -> {
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

            EntityTypeEnum.IF -> {
                this as IfEntity
                stack.add(wrapper)
                val trueElWrapper = ThenELWrapper()
                val falseElWrapper = ThenELWrapper()
                if (this.falseNode!!.type == EntityTypeEnum.SUMMARY) {
                    wrapper.then(ELBus.ifOpt(this.wrap(), trueElWrapper))
                } else {
                    wrapper.then(ELBus.ifOpt(this.wrap(), trueElWrapper, falseElWrapper))
                }
                this.trueNode!!.toEl(trueElWrapper, stack, doneSummary)
                this.falseNode!!.toEl(falseElWrapper, stack, doneSummary)
            }

            EntityTypeEnum.SWITCH -> {
                this as SwitchEntity
                stack.add(wrapper)
                val switchWrapper = ELBus.switchOpt(this.wrap())!!
                wrapper.then(switchWrapper)
                this.tagMap.forEach {
                    val thenWrapper = ThenELWrapper().id(it.value)!!
                    switchWrapper.to(thenWrapper)
                    it.key.toEl(thenWrapper, stack, doneSummary)
                }
            }

            EntityTypeEnum.SUMMARY -> this.name.takeIf { it !in doneSummary }?.let { name ->
                doneSummary.add(name)
                this.negativeNodeList.forEach {
                    it.toEl(stack.removeLast(), stack, doneSummary)
                }
            }

            EntityTypeEnum.START -> this.negativeNodeList.forEach { it.toEl(wrapper, stack, doneSummary) }
            EntityTypeEnum.END -> {} // nothing to do
        }
        return wrapper
    }

    private fun <T : BaseEntity> T.wrap() = ELBus.node(this.name).also {
        if (this is BaseEntityWithData && !this.data.isNullOrBlank()) it.data(randomDataName(this.name), this.data)
    }

    private fun randomDataName(prefix: String): String = "$prefix-${UUID.randomUUID().toString().substring(0, 8)}"

}
