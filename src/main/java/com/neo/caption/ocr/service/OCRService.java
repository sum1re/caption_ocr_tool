package com.neo.caption.ocr.service;

import com.neo.caption.ocr.util.OpenCVUtil;
import lombok.RequiredArgsConstructor;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.Mat;

@RequiredArgsConstructor
public class OCRService {

    private final TessBaseAPI tessBaseAPI;

    public String doOCR(Mat mat) {
        var bytes = OpenCVUtil.mat2ByteArrayByGet(mat);
        tessBaseAPI.SetImage(bytes, mat.cols(), mat.rows(), mat.channels(), mat.cols());
        var bytePointer = tessBaseAPI.GetUTF8Text();
        var text = bytePointer.isNull() ? "" : bytePointer.getString();
        bytePointer.close();
        return text;
    }

    public void close() {
        if (tessBaseAPI != null) {
            tessBaseAPI.close();
        }
    }

}
