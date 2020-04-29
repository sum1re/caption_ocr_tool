package com.neo.caption.ocr.service;

import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.exception.InvalidMatNodesException;
import com.neo.caption.ocr.pojo.ModuleStatus;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public interface FileService {

    void loadModuleProfileStatusList() throws IOException;

    Integer saveAss(File assFile) throws InvalidMatNodesException, IOException;

    Integer saveSrt(File srtFile) throws InvalidMatNodesException, IOException;

    Integer readCOCR(File cocrFile) throws IOException;

    Integer saveCOCR(File cocrFile) throws InvalidMatNodesException, IOException;

    Integer saveOCRImage(File imageFile) throws InvalidMatNodesException, IOException;

    @AopException
    Integer saveOCRText(File txtFile) throws IOException;

    void saveModuleProfile(String profileName, Object object) throws IOException;

    ModuleStatus[] readModuleProfile(String profileName) throws IOException;

    void deleteModuleProfile(String profileName) throws IOException;

    File openFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters);

    List<File> openMultiFileDialog(Stage stage, String title);

    File saveFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters);

    boolean verifyBatFile(File file);

    void saveJsonToFile(Writer writer, Object object) throws IOException;

    <T> T readJsonFromFile(Reader reader, Class<T> tClass) throws IOException;
}
