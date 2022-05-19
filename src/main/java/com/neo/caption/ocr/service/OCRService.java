package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import com.neo.caption.ocr.domain.entity.CaptionRow;
import com.neo.caption.ocr.util.OpenCVUtil;
import org.bytedeco.tesseract.TessBaseAPI;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public record OCRService(TessBaseAPI api) {

    private String doOCR(Mat mat) {
        var bytes = OpenCVUtil.mat2ByteArrayByGet(mat);
        api.SetImage(bytes, mat.cols(), mat.rows(), mat.channels(), mat.cols());
        try (var bytePointer = api.GetUTF8Text()) {
            return bytePointer.isNull() ? "" : bytePointer.getString();
        }
    }

    public List<CaptionRowDto> doOCR(@NotNull List<CaptionRow> captionRowList) {
        var dtoList = new ArrayList<CaptionRowDto>(captionRowList.size());
        for (var captionRow : captionRowList) {
            var mat = captionRow.getMat();
            var caption = doOCR(mat);
            OpenCVUtil.release(mat);
            captionRow.setCaption(caption);
            dtoList.add(captionRow.toDto());
        }
        captionRowList.clear();
        return dtoList;
    }

}
