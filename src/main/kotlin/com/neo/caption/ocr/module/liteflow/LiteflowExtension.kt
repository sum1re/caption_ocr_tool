package com.neo.caption.ocr.module.liteflow

import com.yomahub.liteflow.builder.el.ELBus
import com.yomahub.liteflow.builder.el.ELWrapper
import com.yomahub.liteflow.builder.el.IfELWrapper
import com.yomahub.liteflow.builder.el.LoopELWrapper
import com.yomahub.liteflow.builder.el.NodeELWrapper
import com.yomahub.liteflow.builder.el.SwitchELWrapper
import com.yomahub.liteflow.builder.el.ThenELWrapper
import com.yomahub.liteflow.core.NodeComponent

inline fun <reified T : BaseContext> NodeComponent.context(): T =
    this.getContextBean(T::class.java) ?: throw RuntimeException("context not found")

fun NodeELWrapper.ternary(trueElWrapper: ELWrapper, falseElWrapper: ELWrapper): IfELWrapper =
    ELBus.ifOpt(this, trueElWrapper, falseElWrapper)

fun NodeELWrapper.ifTrue(trueElWrapper: ELWrapper): IfELWrapper = ELBus.ifOpt(this, trueElWrapper)

fun ELWrapper.then(vararg elWrapper: ELWrapper): ThenELWrapper = ELBus.then(this, *elWrapper)

fun NodeELWrapper.whileDo(elWrapper: ELWrapper): LoopELWrapper = ELBus.whileOpt(this).doOpt(elWrapper)

fun <T : Enum<T>> NodeELWrapper.switch(vararg elWrapperWithTag: Pair<T, ELWrapper>): SwitchELWrapper =
    ELBus.switchOpt(this).to(*elWrapperWithTag.map { it.second.tag(it.first.name) }.toTypedArray())

fun NodeELWrapper.iterator(elWrapper: ELWrapper): LoopELWrapper = ELBus.iteratorOpt(this).doOpt(elWrapper)