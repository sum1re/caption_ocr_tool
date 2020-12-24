package com.neo.caption.ocr.service.impl;

import com.google.common.base.*;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.constant.FileType;
import com.neo.caption.ocr.exception.InvalidMatNodesException;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.COCRData;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.OpenCVService;
import com.neo.caption.ocr.service.PreferencesService;
import com.neo.caption.ocr.view.MatNode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.neo.caption.ocr.constant.Dir.MODULE_PROFILE_DIR;
import static com.neo.caption.ocr.constant.Dir.TEMP_DIR;
import static com.neo.caption.ocr.constant.FileType.VIDEO;
import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.BaseUtil.convertTime;
import static com.neo.caption.ocr.util.BaseUtil.v2s;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final OpenCVService openCVService;
    private final Gson gson;
    private final AppHolder appHolder;
    private final PreferencesService preferencesService;
    private final Splitter splitter;
    private final CharMatcher charMatcher;
    private final Joiner joiner;

    private String fileChooseInitName;

    public FileServiceImpl(OpenCVService openCVService, Gson gson, AppHolder appHolder,
                           PreferencesService preferencesService, @Qualifier("lineSeparator") Splitter splitter,
                           CharMatcher charMatcher, @Qualifier("dot") Joiner joiner) {
        this.openCVService = openCVService;
        this.gson = gson;
        this.appHolder = appHolder;
        this.preferencesService = preferencesService;
        this.splitter = splitter;
        this.charMatcher = charMatcher;
        this.joiner = joiner;
    }

    @PostConstruct
    public void init() throws IOException {
        if (!MODULE_PROFILE_DIR.exists()) {
            Files.createDirectory(MODULE_PROFILE_DIR.toPath());
        }
        if (!TEMP_DIR.exists()) {
            Files.createDirectory(TEMP_DIR.toPath());
        }
        listProfile();
        List<String> tempList = appHolder.getModuleProfileList();
        String tempName = MODULE_PROFILE_NAME.stringValue();
        if (Strings.isNullOrEmpty(tempName) || tempList.isEmpty() || !tempList.contains(tempName)) {
            if (!tempList.contains("Default")) {
                tempList.add("Default");
                saveModuleProfile("Default", MODULE_PROFILE_DEFAULT.value());
            }
            preferencesService.put(MODULE_PROFILE_NAME, "Default");
        }
        loadModuleProfileStatusList();
    }

    @Override
    @AopException
    public void loadModuleProfileStatusList() throws IOException {
        ModuleStatus[] moduleStatusList = readModuleProfile(MODULE_PROFILE_NAME.stringValue());
        MODULE_PROFILE_STATUS_LIST.setValue(new ArrayList<>(Arrays.asList((moduleStatusList))));
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
                .append(DEFAULT_STYLE.stringValue())
                .append(System.lineSeparator())
                .append("[Events]")
                .append(System.lineSeparator())
                .append("Format: Layer,Start,End,Style,Name,MarginL,MarginR,MarginV,Effect,Text")
                .append(System.lineSeparator());
        while (nodeIterator.hasNext()) {
            MatNode matNode = nodeIterator.next();
            stringBuilder.append("Dialogue: 0,")
                    .append(convertTime(matNode.getStartTime()))
                    .append(",")
                    .append(convertTime(matNode.getEndTime()))
                    .append(",Default,,0,0,0,,")
                    .append(ocrIterator.hasNext() ? ocrIterator.next() : "")
                    .append(System.lineSeparator());
        }
        try (FileOutputStream fos = new FileOutputStream(assFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, UTF_8)) {
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
                    .append(charMatcher.replaceFrom(convertTime(matNode.getStartTime()), ','))
                    .append(" --> ")
                    .append(charMatcher.replaceFrom(convertTime(matNode.getEndTime()), ','))
                    .append(System.lineSeparator())
                    .append(ocrIterator.hasNext() ? ocrIterator.next() : "")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            index++;
        }
        try (FileOutputStream fos = new FileOutputStream(srtFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, UTF_8)) {
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
    @SuppressWarnings("UnstableApiUsage")
    public Integer saveOCRImage(File imageFile, int size) throws InvalidMatNodesException, IOException {
        verify();
        final String fileFullName = imageFile.getName();
        final String fileName = com.google.common.io.Files.getNameWithoutExtension(fileFullName);
        final String fileExt = com.google.common.io.Files.getFileExtension(fileFullName);
        final File tempFile = new File(TEMP_DIR, joiner.join("cache", fileExt));
        File outFile;
        final List<Mat> matList = openCVService.spliceMatList(size);
        final int digital = v2s(matList.size()).length();
        for (int i = 0, len = matList.size(); i < len; i++) {
            final String outName = joiner.join(fileName, Strings.padStart(v2s(i + 1), digital, '0'));
            outFile = new File(imageFile.getParent(), joiner.join(outName, fileExt));
            final boolean result = Imgcodecs.imwrite(tempFile.getAbsolutePath(), matList.get(i));
            if (!result) {
                return 0;
            }
            Files.copy(tempFile.toPath(), outFile.toPath(), REPLACE_EXISTING);
            matList.get(i).release();
        }
        Files.delete(tempFile.toPath());
        return 1;
    }

    @Override
    @AopException
    public Integer saveOCRText(File txtFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(txtFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, UTF_8)) {
            writer.append(appHolder.getOcr());
        }
        return 1;
    }

    @Override
    @AopException
    @SuppressWarnings("UnstableApiUsage")
    public void saveModuleProfile(String profileName, Object object) throws IOException {
        File profile = new File(MODULE_PROFILE_DIR, joiner.join(profileName, "json"));
        try (BufferedWriter bufferedWriter = com.google.common.io.Files.newWriter(profile, UTF_8)) {
            saveJsonToFile(bufferedWriter, object);
        }
    }

    @Override
    @AopException
    @SuppressWarnings("UnstableApiUsage")
    public ModuleStatus[] readModuleProfile(String profileName) throws IOException {
        File profile = new File(MODULE_PROFILE_DIR, joiner.join(profileName, "json"));
        if (!profile.exists()) {
            log.info("ProfileNotFound: {}", profile);
            throw new FileNotFoundException();
        }
        try (BufferedReader bufferedReader = com.google.common.io.Files.newReader(profile, UTF_8)) {
            return readJsonFromFile(bufferedReader, ModuleStatus[].class);
        }
    }

    @Override
    @AopException
    public void deleteModuleProfile(String profileName) throws IOException {
        File profile = new File(MODULE_PROFILE_DIR, joiner.join(profileName, "json"));
        if (!profile.exists()) {
            log.info("ProfileNotFound: {}", profile);
            throw new FileNotFoundException();
        }
        Files.delete(profile.toPath());
    }

    @Override
    public File openFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = initFileChooser(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            preferencesService.put(FILE_CHOOSE_DIR, file.getParent());
            initFileName(file);
        }
        return file;
    }

    @Override
    public List<File> openMultiFileDialog(Stage stage, String title) {
        FileChooser fileChooser = initFileChooser(title);
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        if (list != null && !list.isEmpty()) {
            preferencesService.put(FILE_CHOOSE_DIR, list.get(0).getParent());
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
            preferencesService.put(FILE_CHOOSE_DIR, file.getParent());
            initFileName(file);
        }
        return file;
    }

    @Override
    public boolean verifyBatFile(File file) {
        if (file == null) {
            return false;
        }
        @SuppressWarnings("UnstableApiUsage")
        String ext = com.google.common.io.Files.getFileExtension(file.getName());
        if (Strings.isNullOrEmpty(ext)) {
            return false;
        }
        ext = "*." + ext;
        return Set.of(VIDEO.getExtensions()).contains(ext) || Set.of(FileType.COCR.getExtensions()).contains(ext);
    }

    @Override
    public void saveJsonToFile(Writer writer, Object object) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(writer)) {
            gson.toJson(object, object.getClass(), jsonWriter);
        }
    }

    @Override
    public <T> T readJsonFromFile(Reader reader, Class<T> tClass) throws IOException {
        try (JsonReader jsonReader = new JsonReader(reader)) {
            return gson.fromJson(jsonReader, tClass);
        }
    }

    @Override
    @AopException
    public String getFileHeader(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[4];
            //noinspection ResultOfMethodCallIgnored
            fis.read(bytes, 0, bytes.length);
            return bytesToHex(bytes);
        }
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
             OutputStreamWriter osw = new OutputStreamWriter(gzip, UTF_8)) {
            saveJsonToFile(osw, new COCR(list, ocr));
        }
    }

    private COCR extract(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzip = new GZIPInputStream(fis);
             InputStreamReader isr = new InputStreamReader(gzip, UTF_8)) {
            return readJsonFromFile(isr, COCR.class);
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
        if (!isNullOrEmpty(FILE_CHOOSE_DIR.stringValue())) {
            File dir = new File(FILE_CHOOSE_DIR.stringValue());
            if (dir.exists()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        return fileChooser;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void listProfile() {
        File[] files = MODULE_PROFILE_DIR.listFiles();
        if (files == null) {
            return;
        }
        appHolder.setModuleProfileList(Arrays.stream(files)
                .filter((Predicate<File>) file -> file != null && file.getName().endsWith(".json"))
                .map(file -> com.google.common.io.Files.getNameWithoutExtension(file.getName()))
                .collect(Collectors.toList()));
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder builder = appHolder.getStringBuilder();
        builder.setLength(0);
        String hex;
        for (byte b : bytes) {
            hex = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hex.length() < 2)
                builder.append(0);
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * COCR data object
     */
    @Getter
    @AllArgsConstructor
    private static class COCR implements Serializable {

        private static final long serialVersionUID = 7943334895304386183L;

        private final List<COCRData> list;
        private final String ocr;
    }
}
