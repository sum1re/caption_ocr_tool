package com.neo.caption.ocr.service

import org.opencv.videoio.VideoCapture

interface VideoService {

    fun openVideo(videoAbsolutePath: String): VideoCapture

    fun processVideo(identity: String)

}