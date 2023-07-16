package com.neo.caption.ocr

import org.opencv.core.Rect

fun Rect.isXConnected(other: Rect) = this.x <= other.x && this.brx() <= other.brx()

fun Rect.isXContains(other: Rect, offset: Int = 0) = this.xRange(offset).let { other.x in it && other.brx() in it }

fun Rect.isXContainsAny(other: Rect, offset: Int = 0) =
    this.isXContains(other, offset) || other.isXContains(this, offset)

fun Rect.isYConnected(other: Rect) = this.y <= other.y && this.bry() <= other.bry()

fun Rect.isYContains(other: Rect, offset: Int = 0) = this.yRange(offset).let { other.y in it && other.bry() in it }

fun Rect.isYContainsAny(other: Rect, offset: Int = 0) =
    this.isYContains(other, offset) || other.isYContains(this, offset)

fun Rect.brx() = this.x + this.width

fun Rect.bry() = this.y + this.height

private fun Rect.xRange(offset: Int) = (this.x - offset)..(this.brx() + offset)

private fun Rect.yRange(offset: Int) = (this.y - offset)..(this.bry() + offset)