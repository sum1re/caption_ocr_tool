package com.neo.caption.ocr.service.impl;

import com.google.common.base.Splitter;
import com.neo.caption.ocr.domain.dto.TessLanguageDto;
import com.neo.caption.ocr.property.TesseractProperties;
import com.neo.caption.ocr.service.TesseractService;
import com.neo.caption.ocr.util.BaseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.tesseract.StringVector;
import org.bytedeco.tesseract.TessBaseAPI;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TesseractServiceImpl implements TesseractService {

    private final TesseractProperties tesseractProperties;
    private final Splitter commaSplitter;

    private Path tessDataPath;
    private Path tessConfigPath;
    private List<TessLanguageDto> tessLanguageDtoList;

    @PostConstruct
    public void init() {
        this.tessDataPath = Paths.get("").resolve("app").resolve("tessdata");
        if (!Files.exists(tessDataPath)) {
            log.warn("The tessdata does not exist: {}", tessDataPath.toAbsolutePath());
            this.tessLanguageDtoList = Collections.emptyList();
            return;
        }
        this.tessConfigPath = tessDataPath.resolve("config");
        var languageList = commaSplitter.splitToList(tesseractProperties.getSupportedLanguage());
        if (languageList.isEmpty()) {
            log.warn("not set the supported languages");
            this.tessLanguageDtoList = Collections.emptyList();
            return;
        }
        this.tessLanguageDtoList = new ArrayList<>(languageList.size());
        languageList.forEach(this::addToTessLanguageDtoList);
    }

    @Override
    public TessBaseAPI initTessBaseApi(String language) {
        try (var tessBaseAPI = new TessBaseAPI();
             var variableName = new StringVector();
             var variableValue = new StringVector()) {
            for (var config : tesseractProperties.getConfigs()) {
                variableName.put(config.getName());
                variableValue.put(config.getValue());
            }
            variableName.put("tessedit_pageseg_mode");
            variableValue.put(BaseUtil.v2s(tesseractProperties.getPageSegMode()));
            tessBaseAPI.Init(tessDataPath.toAbsolutePath().toString(),
                    language,
                    tesseractProperties.getOcrEngineMode(),
                    new byte[0],
                    (int) variableName.size(),
                    variableName,
                    variableValue,
                    true);
            if (Files.exists(tessConfigPath)) {
                tessBaseAPI.ReadConfigFile(tessConfigPath.toAbsolutePath().toString());
            }
            return tessBaseAPI;
        }
    }

    @Override
    public List<TessLanguageDto> getTessLanguageDtoList() {
        return tessLanguageDtoList;
    }

    private void addToTessLanguageDtoList(String language) {
        var dataPath = tessDataPath.resolve(language + ".traineddata");
        var dto = new TessLanguageDto(language, Files.exists(dataPath));
        tessLanguageDtoList.add(dto);
    }

}
