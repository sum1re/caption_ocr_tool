package com.neo.caption.ocr.service.impl;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.exception.InvalidMatNodesException;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.COCRData;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.OpenCVService;
import com.neo.caption.ocr.util.PrefUtil;
import com.neo.caption.ocr.view.MatNode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.neo.caption.ocr.util.BaseUtil.convertTime;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final OpenCVService openCVService;
    private final Gson gson;
    private final AppHolder appHolder;
    private final PrefUtil prefUtil;
    private final Splitter splitter;
    private final CharMatcher charMatcher;

    private String fileChooseInitName;

    public FileServiceImpl(OpenCVService openCVService, Gson gson, AppHolder appHolder,
                           PrefUtil prefUtil, @Qualifier("lineSeparator") Splitter splitter,
                           CharMatcher charMatcher) {
        this.openCVService = openCVService;
        this.gson = gson;
        this.appHolder = appHolder;
        this.prefUtil = prefUtil;
        this.splitter = splitter;
        this.charMatcher = charMatcher;
    }

    @Override
    @AopException
    public Integer saveAss(File assFile) throws InvalidMatNodesException, IOException {
        verify();
        UnmodifiableIterator<String> ocrIterator = splitter.splitToList(appHolder.getOcr())
                .stream()
                .collect(toImmutableList())
                .iterator();
        UnmodifiableIterator<MatNode> nodeIterator = appHolder.getMatNodeList()
                .stream()
                .collect(toImmutableList())
                .iterator();
        StringBuilder stringBuilder = appHolder.getStringBuilder();
        stringBuilder.append("[Script Info]")
                .append(System.lineSeparator())
                .append("Title: untitled")
                .append(System.lineSeparator())
                .append("ScriptType: v4.00+")
                .append(System.lineSeparator())
                .append("WrapStyle: 0")
                .append(System.lineSeparator())
                .append("ScaledBorderAndShadow: yes")
                .append(System.lineSeparator())
                .append("[V4+ Styles]")
                .append(System.lineSeparator())
                .append("Format: Name,Fontname,Fontsize,PrimaryColour,SecondaryColour,OutlineColour,")
                .append("BackColour,Bold,Italic,Underline,StrikeOut,ScaleX, ScaleY,Spacing,Angle,")
                .append("BorderStyle,Outline,Shadow,Alignment,MarginL,MarginR,MarginV,Encoding")
                .append(System.lineSeparator())
                .append(prefUtil.getDefaultStyle())
                .append(System.lineSeparator())
                .append("[Events]")
                .append(System.lineSeparator())
                .append("Format: Layer,Start,End,Style,Name,MarginL,MarginR,MarginV,Effect,Text")
                .append(System.lineSeparator());
        while (nodeIterator.hasNext()) {
            MatNode matNode = nodeIterator.next();
            log.info("to ass: {}", matNode);
            stringBuilder.append("Dialogue: 0,")
                    .append(convertTime(matNode.getStartTime()))
                    .append(",")
                    .append(convertTime(matNode.getEndTime()))
                    .append(",Default,,0,0,0,,")
                    .append(ocrIterator.hasNext() ? ocrIterator.next() : "")
                    .append(System.lineSeparator());
        }
        try (FileOutputStream fos = new FileOutputStream(assFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.append(stringBuilder);
        }
        return 1;
    }

    @Override
    @AopException
    public Integer saveSrt(File srtFile) throws InvalidMatNodesException, IOException {
        verify();
        UnmodifiableIterator<String> ocrIterator = splitter.splitToList(appHolder.getOcr())
                .stream()
                .collect(toImmutableList())
                .iterator();
        UnmodifiableIterator<MatNode> nodeIterator = appHolder.getMatNodeList()
                .stream()
                .collect(toImmutableList())
                .iterator();
        StringBuilder stringBuilder = appHolder.getStringBuilder();
        int index = 0;
        while (nodeIterator.hasNext()) {
            MatNode matNode = nodeIterator.next();
            stringBuilder.append(index + 1)
                    .append(System.lineSeparator())
                    .append(charMatcher.replaceFrom(convertTime(matNode.getStartTime()),','))
                    .append(" --> ")
                    .append(charMatcher.replaceFrom(convertTime(matNode.getEndTime()),','))
                    .append(System.lineSeparator())
                    .append(ocrIterator.hasNext() ? ocrIterator.next() : "")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            index++;
        }
        try (FileOutputStream fos = new FileOutputStream(srtFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.append(stringBuilder);
        }
        return 1;
    }

    @Override
    @AopException
    public Integer readCOCR(File cocrFile) throws IOException {
        COCR cocr = extract(cocrFile);
        appHolder.setMatNodeList(cocr.getList()
                .stream()
                .map(COCRData::cvtToMatNode)
                .collect(Collectors.toList()));
        appHolder.setOcr(cocr.getOcr());
        return 1;
    }

    @Override
    @AopException
    public Integer saveCOCR(File cocrFile) throws InvalidMatNodesException, IOException {
        verify();
        List<COCRData> list = appHolder.getMatNodeList()
                .stream()
                .map(matNode -> {
                    COCRData cocrData = new COCRData();
                    cocrData.setId(matNode.getNid())
                            .setStartTime(matNode.getStartTime())
                            .setEndTime(matNode.getEndTime())
                            .setCols(matNode.getMat().cols())
                            .setRows(matNode.getMat().rows())
                            .setType(matNode.getMat().type())
                            .setMatByte(openCVService.mat2ByteArrayByGet(matNode.getMat()));
                    return cocrData;
                })
                .collect(Collectors.toList());
        compress(cocrFile, list, appHolder.getOcr());
        return 1;
    }

    @Override
    @AopException
    public Integer saveOCRImage(File imageFile) throws InvalidMatNodesException, IOException {
        verify();
        Mat mat = openCVService.spliceMatList();
        Files.write(imageFile.toPath(), openCVService.mat2ByteArrayByMatOfByte(mat));
        mat.release();
        return 1;
    }

    @Override
    public File openFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = initFileChooser(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            prefUtil.setFileChooseDir(file.getParent());
            initFileName(file);
        }
        return file;
    }

    @Override
    public List<File> openMultiFileDialog(Stage stage, String title) {
        FileChooser fileChooser = initFileChooser(title);
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        if (list != null && !list.isEmpty()) {
            prefUtil.setFileChooseDir(list.get(0).getParent());
        }
        return list;
    }

    @Override
    public File saveFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = initFileChooser(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        if (!isNullOrEmpty(fileChooseInitName)) {
            fileChooser.setInitialFileName(fileChooseInitName);
        }
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            prefUtil.setFileChooseDir(file.getParent());
            initFileName(file);
        }
        return file;
    }

    @Override
    public boolean verifyBatFile(File file) {
        if (file == null) {
            return false;
        }
        String fileName = file.getName();
        if (!fileName.contains(".")) {
            return false;
        }
        String str = fileName.substring(fileName.lastIndexOf("."));
        return prefUtil.getDigitalContainerFormat().contains(str) || ".cocr".contains(str);
    }

    private void initFileName(File file) {
        fileChooseInitName = file.getName();
        if (fileChooseInitName.contains(".")) {
            fileChooseInitName = fileChooseInitName.substring(0, fileChooseInitName.lastIndexOf("."));
        }
    }

    private void compress(File file, List<COCRData> list, String ocr) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             GZIPOutputStream gzip = new GZIPOutputStream(fos);
             OutputStreamWriter osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
             JsonWriter writer = new JsonWriter(osw)) {
            gson.toJson(new COCR(list, ocr), COCR.class, writer);
        }
    }

    private COCR extract(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzip = new GZIPInputStream(fis);
             InputStreamReader isr = new InputStreamReader(gzip, StandardCharsets.UTF_8);
             JsonReader jsonReader = new JsonReader(isr)) {
            return gson.fromJson(jsonReader, COCR.class);
        }
    }

    private void verify() throws InvalidMatNodesException {
        if (appHolder.getMatNodeList().isEmpty()) {
            throw new InvalidMatNodesException("MatNode list is null or empty.");
        }
    }

    private FileChooser initFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (!isNullOrEmpty(prefUtil.getFileChooseDir())) {
            File dir = new File(prefUtil.getFileChooseDir());
            if (dir.exists()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        return fileChooser;
    }

    /**
     * COCR data object
     */
    @Getter
    @AllArgsConstructor
    private static class COCR implements Serializable {

        private static final long serialVersionUID = 7943334895304386183L;

        private List<COCRData> list;
        private String ocr;
    }
}
