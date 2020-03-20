package com.neo.caption.ocr.service;

import com.neo.caption.ocr.exception.InvalidMatNodesException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileService {

    File openFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters);

    List<File> openMultiFileDialog(Stage stage, String title);

    File saveFileDialog(Stage stage, String title, FileChooser.ExtensionFilter... extensionFilters);

    Integer saveAss(File assFile) throws InvalidMatNodesException, IOException;

    Integer saveSrt(File srtFile) throws InvalidMatNodesException, IOException;

    Integer readCOCR(File cocrFile) throws IOException;

    Integer saveCOCR(File cocrFile) throws InvalidMatNodesException, IOException;

    Integer saveOCRImage(File imageFile) throws InvalidMatNodesException, IOException;

    boolean verifyBatFile(File file);
}
