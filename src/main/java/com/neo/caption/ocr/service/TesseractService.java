package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.dto.TessLanguageDto;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.List;

public interface TesseractService {

    TessBaseAPI initTessBaseApi(String language);

    List<TessLanguageDto> getTessLanguageDtoList();

}
