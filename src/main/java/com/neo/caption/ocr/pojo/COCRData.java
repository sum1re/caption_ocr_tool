package com.neo.caption.ocr.pojo;

import com.neo.caption.ocr.view.MatNode;
import lombok.Data;
import lombok.experimental.Accessors;
import org.opencv.core.Mat;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class COCRData implements Serializable {

    private static final long serialVersionUID = 8341558515796456597L;

    private int cols;
    private int rows;
    private int type;
    private int id;
    private double startTime;
    private double endTime;
    private byte[] matByte;

    public MatNode cvtToMatNode() {
        Mat mat = new Mat(rows, cols, type);
        mat.put(0, 0, matByte);
        return new MatNode(id, startTime, endTime, mat);
    }
}
