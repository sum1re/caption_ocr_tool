package com.neo.caption.ocr.service;

import com.neo.caption.ocr.util.OpenCVUtil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.Mat;

public class OCRService {

    private final TessBaseAPI tessBaseAPI;
    private BytePointer bytePointer;

    public OCRService(TessBaseAPI tessBaseAPI) {
        this.tessBaseAPI = tessBaseAPI;
        this.bytePointer = null;
    }

    public String doOCR(Mat mat) {
        var bytes = OpenCVUtil.mat2ByteArrayByGet(mat);
        tessBaseAPI.SetImage(bytes, mat.cols(), mat.rows(), mat.channels(), mat.cols());
        bytePointer = tessBaseAPI.GetUTF8Text();
        return bytePointer.isNull() ? "" : bytePointer.getString();

    }

    public void close() {
        if (tessBaseAPI != null) {
            tessBaseAPI.close();
        }
        if (bytePointer != null) {
            bytePointer.close();
        }
    }

}
