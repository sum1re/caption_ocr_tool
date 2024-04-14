package com.neo.caption.ocr.module.liteflow

import com.yomahub.liteflow.core.NodeComponent

inline fun <reified T : BaseContext> NodeComponent.context(): T =
    this.getContextBean(T::class.java) ?: throw RuntimeException("context not found")
