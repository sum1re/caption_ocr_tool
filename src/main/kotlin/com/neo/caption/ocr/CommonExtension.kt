package com.neo.caption.ocr

import org.opencv.core.Mat

fun Mat.toByteArray() = ByteArray(this.caleChannels().toInt()).also { this.get(0, 0, it) }

private fun Mat.caleChannels(): Long = this.total() * this.channels()