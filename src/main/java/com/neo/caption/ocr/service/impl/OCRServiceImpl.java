package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.exception.TessException;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.OCRService;
import com.neo.caption.ocr.service.OpenCVService;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.util.PrefUtil;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ResourceBundle;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@Slf4j
public class OCRServiceImpl implements OCRService {

    private final ResourceBundle resourceBundle;
    private final OpenCVService openCVService;
    private final TessBaseAPI api;
    private final FxUtil fxUtil;
    private final AppHolder appHolder;
    private final PrefUtil prefUtil;

    private boolean ready;

    public OCRServiceImpl(ResourceBundle resourceBundle, OpenCVService openCVService, TessBaseAPI api,
                          FxUtil fxUtil, AppHolder appHolder, PrefUtil prefUtil) {
        this.resourceBundle = resourceBundle;
        this.openCVService = openCVService;
        this.api = api;
        this.fxUtil = fxUtil;
        this.appHolder = appHolder;
        this.prefUtil = prefUtil;
    }

    @PostConstruct
    public void init(){
        this.ready = false;
    }

    @Override
    @AopException
    public void apiInit() throws TessException {
        if (!TESS_DATA_DIR.exists()) {
            throw new TessException(resourceBundle.getString("exception.msg.tess.data.miss"));
        }
        if (api.Init(TESS_DATA_DIR.getAbsolutePath(), TESS_LANG.stringValue()) != 0) {
            throw new TessException(resourceBundle.getString("exception.msg.tess.init"));
        }
        api.ReadConfigFile(new File(TESS_DATA_DIR, "config").getAbsolutePath());
        ready = true;
    }

    @Override
    public String doOCR(Mat mat) {
        // convert OpenCV::Mat to Tesseract::Pix
        api.SetImage(openCVService.mat2ByteArrayByGet(mat), mat.cols(), mat.rows(), mat.channels(), mat.cols());
        BytePointer bp = api.GetUTF8Text();
        return bp == null ? "" : bp.getString();
    }

    @Override
    public Integer doOCR(ProgressBar jfxProgressBar) {
        StringBuilder stringBuilder = appHolder.getStringBuilder();
        for (int i = 0, len = appHolder.getMatNodeList().size(), n = len - 1; i < len; i++) {
            fxUtil.onFXThread(jfxProgressBar.progressProperty(), (double) i / n);
            stringBuilder.append(doOCR(appHolder.getMatNodeList().get(i).getMat()));
        }
        appHolder.setOcr(stringBuilder.toString());
        return 1;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
