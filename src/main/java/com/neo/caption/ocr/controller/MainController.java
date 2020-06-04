package com.neo.caption.ocr.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.neo.caption.ocr.constant.LayoutName;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.Build;
import com.neo.caption.ocr.service.*;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.AsyncTask;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.view.MatNode;
import com.neo.caption.ocr.view.Toast;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.DecimalUtil.divide;
import static javafx.scene.layout.BackgroundPosition.CENTER;
import static javafx.scene.layout.BackgroundRepeat.NO_REPEAT;

@Controller
@Slf4j
@Lazy
public class MainController implements BaseController {

    @FXML
    public VBox root;
    @FXML
    public MenuBar menu_bar;
    @FXML
    public VBox mask;
    @FXML
    public TextArea text_area;
    @FXML
    public ScrollPane scroll_pane;
    @FXML
    public FlowPane flow_pane;
    @FXML
    public Button btn_start;
    @FXML
    public ProgressBar progress_bar;
    @FXML
    public Slider slider_zoom;
    @FXML
    public CheckMenuItem check_manager;
    @FXML
    public Label file_name;
    @FXML
    public Label frame_time;

    private final MatNodeService matNodeService;
    private final FileService fileService;
    private final OpenCVService openCVService;
    private final VideoService videoService;
    private final OCRService ocrService;
    private final StageService stageService;
    private final StageBroadcast stageBroadcast;
    private final FxUtil fxUtil;
    private final ResourceBundle resourceBundle;
    private final AppHolder appHolder;
    private final PreferencesService preferencesService;
    private final ExecutorService service;

    private final static String MASK_LIGHT_STYLE = "-fx-background-color: rgba(255,255,255,%1$f)";
    private final static String MASK_DARK_STYLE = "-fx-background-color: rgba(37,37,37,%1$f)";

    private Stage stage;
    private ToggleGroup group;

    private AsyncTask commonAsyncTask;
    private AsyncTask videoAsyncTask;
    private File cocrFile;
    private File assFile;
    private int scrollPaneVolume;
    private double scrollPaneWidth;
    private double matNodeHeight;
    private int oldLine;

    public MainController(OCRService ocrService, MatNodeService matNodeService, PreferencesService preferencesService,
                          FileService fileService, OpenCVService openCVService, VideoService videoService,
                          StageService stageService, StageBroadcast stageBroadcast, FxUtil fxUtil,
                          ExecutorService service, ResourceBundle resourceBundle,
                          AppHolder appHolder) {
        this.ocrService = ocrService;
        this.matNodeService = matNodeService;
        this.fileService = fileService;
        this.openCVService = openCVService;
        this.videoService = videoService;
        this.stageService = stageService;
        this.stageBroadcast = stageBroadcast;
        this.fxUtil = fxUtil;
        this.service = service;
        this.resourceBundle = resourceBundle;
        this.appHolder = appHolder;
        this.preferencesService = preferencesService;
    }

    @Override
    public void init() {
        this.group = new ToggleGroup();
        this.scrollPaneVolume = 0;
        this.matNodeHeight = 0;
        this.oldLine = 0;
        this.commonAsyncTask = new AsyncTask();
        this.videoAsyncTask = new AsyncTask();
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            stageService.remove(stage);
            //do nothing
        });
    }

    @Override
    public void delay() {
        this.stage = stageService.add(root);
        fxUtil.setFontSize(text_area, EDITOR_FONT_SIZE.intValue());
        this.scrollPaneWidth = scroll_pane.getWidth();
        onBackgroundModify();
    }

    @Override
    public void bindListener() {
        text_area.caretPositionProperty().addListener(this::onTextAreaPositionModify);
        scroll_pane.heightProperty().addListener((ov, a, b) -> scrollPaneVolume = 0);
        scroll_pane.vvalueProperty().addListener(this::onScrollPaneVModify);
        group.selectedToggleProperty().addListener(this::onGroupItemSelected);
        slider_zoom.valueProperty().addListener(this::onSliderModify);
        check_manager.selectedProperty().addListener(this::onManagerModify);
        stageBroadcast.digitalBroadcast().addListener((ov, a, b) -> appHolder.loadVideoFilter());
        stageBroadcast.tessLangBroadcast().addListener(this::onTessLanguageModify);
        stageBroadcast.dataEmptyBroadcast().addListener((ov, a, b) -> clearMatNodeAndOCR());
        stageBroadcast.editorBroadcast().addListener((ov, a, b) -> fxUtil.setFontSize(text_area, b.intValue()));
        stageBroadcast.backgroundImageBroadcast().addListener((ov, a, b) -> onBackgroundModify());
        root.setOnDragOver(e -> e.acceptTransferModes(TransferMode.ANY));
    }

    @Override
    public void bindHotKey() {
        Scene scene = stage.getScene();
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D),
                this::onDelMergeClick);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.C),
                this::removeBeginTag);
    }

    @FXML
    public void onOpenClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        cocrFile = fileService.openFileDialog(stage,
                resourceBundle.getString("file.choose.cocr"),
                appHolder.getCocrFilter());
        if (cocrFile == null) {
            return;
        }
        openCOCR();
    }

    @FXML
    public void onVideoClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        File videoFile = fileService.openFileDialog(stage,
                resourceBundle.getString("file.choose.video"),
                appHolder.getVideoFilter(), appHolder.getNoneFilter());
        if (videoFile == null || !videoFile.exists()) {
            return;
        }
        openVideo(videoFile);
    }

    @FXML
    public void onSaveClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        if (cocrFile == null) {
            cocrFile = fileService.saveFileDialog(stage,
                    resourceBundle.getString("file.choose.save"),
                    appHolder.getCocrFilter());
            if (cocrFile == null) {
                return;
            }
        }
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                appHolder.setOcr(text_area.getText());
                try {
                    return fileService.saveCOCR(cocrFile);
                } catch (Throwable ignored) {
                    return 0;
                }
            }

            @Override
            public void onResult(Integer result) {

            }
        });
        service.submit(commonAsyncTask);
    }

    @FXML
    public void onSaveAsClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        if (assFile == null) {
            assFile = fileService.saveFileDialog(stage,
                    resourceBundle.getString("file.choose.save"),
                    appHolder.getCaptionFilter());
            if (assFile == null) {
                return;
            }
        }
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                appHolder.setOcr(text_area.getText());
                try {
                    return assFile.getName().endsWith(".srt")
                            ? fileService.saveSrt(assFile)
                            : fileService.saveAss(assFile);
                } catch (Throwable ignored) {
                    return 0;
                }
            }

            @Override
            public void onResult(Integer result) {

            }
        });
        service.submit(commonAsyncTask);
    }

    @FXML
    public void onExportClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        File export = fileService.saveFileDialog(stage,
                resourceBundle.getString("file.choose.export"),
                appHolder.getPngFilter());
        if (export == null) {
            return;
        }
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                try {
                    return fileService.saveOCRImage(export);
                } catch (Throwable ignored) {
                    return 0;
                }
            }

            @Override
            public void onResult(Integer result) {

            }
        });
        service.submit(commonAsyncTask);
    }

    @FXML
    public void onDelMergeClick() {
        if (!check_manager.isSelected()) {
            return;
        }
        service.execute(() -> matNodeService.handleDeleteAndMergeTag(flow_pane));
    }

    @FXML
    public void onOCRClick() {
        if (isOtherTaskRunning()) {
            return;
        }
        if (!isNullOrEmpty(text_area.getText())) {
            Optional<ButtonType> result = fxUtil.alertWithCancel(stage,
                    resourceBundle.getString("alert.title.user.warning"),
                    null,
                    resourceBundle.getString("alert.content.ocr.warn"));
            if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                try {
                    if (!ocrService.isReady()) {
                        ocrService.apiInit();
                    }
                    return ocrService.doOCR(progress_bar);
                } catch (Throwable ignored) {
                    return 0;
                }
            }

            @Override
            public void onResult(Integer result) {
                if (result == 1) {
                    text_area.setText(appHolder.getOcr());
                }
            }
        });
        service.submit(commonAsyncTask);
    }

    @FXML
    public void onAboutClick() throws IOException {
        try (InputStream ips = getClass().getResourceAsStream("/ThirdLicense");
             InputStreamReader reader = new InputStreamReader(ips, Charsets.UTF_8)) {
            fxUtil.alert(stage,
                    Build.Info.NAME.value(),
                    Build.Info.VERSION.value(),
                    Build.Info.DESCRIPTION.value(),
                    Collections.singletonList(CharStreams.toString(reader)));
        }
    }

    @FXML
    public void onStartClick() {
        if (commonAsyncTask.isRunning()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.wait"));
            return;
        }
        if (!videoService.isVideoLoaded()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.empty.video"));
            return;
        }
        if (videoService.isVideoFinish()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.completed.video"));
            return;
        }
        if (videoAsyncTask.isRunning()) {
            videoAsyncTask.cancel(true);
        } else {
            videoAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
                @Override
                public void onPreExecute() {
                    Platform.runLater(() -> btn_start.setText(resourceBundle.getString("main.pause")));
                }

                @Override
                public void onPostExecute() {
                    fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
                }

                @Override
                public Integer call() {
                    try {
                        videoService.videoToCOCR(progress_bar);
                        return 1;
                    } catch (Throwable ignored) {
                        return 0;
                    }
                }

                @Override
                public void cancelled() {
                    fxUtil.onFXThread(btn_start.textProperty(), resourceBundle.getString(
                            videoService.isVideoFinish() ? "main.done" : "main.resume"));
                    if (appHolder.getCount() != 0 &&
                            flow_pane.getChildren().size() <= scrollPaneVolume) {
                        nextPage();
                    }
                }

                @Override
                public void onResult(Integer result) {
                    if (result == 1) {
                        if (appHolder.getCount() != 0 &&
                                flow_pane.getChildren().size() <= scrollPaneVolume) {
                            nextPage();
                        }
                        btn_start.setText(resourceBundle.getString(videoService.isVideoFinish() ? "main.done" : "main.resume"));
                    } else {
                        btn_start.setText(resourceBundle.getString("main.start"));
                    }
                }
            });
            service.submit(videoAsyncTask);
        }
    }

    @FXML
    public void onDragDropped(DragEvent dragEvent) {
        if (isOtherTaskRunning()) {
            return;
        }
        Dragboard dragboard = dragEvent.getDragboard();
        if (dragboard.hasFiles()) {
            File file = dragboard.getFiles().get(0);
            if (!fileService.verifyBatFile(file)) {
                Toast.makeToast(stage, resourceBundle.getString("snackbar.invalid.file"));
                return;
            }
            if (file.getName().endsWith(".cocr")) {
                cocrFile = file;
                openCOCR();
            } else {
                openVideo(file);
            }
            preferencesService.put(FILE_CHOOSE_DIR, file.getParent());
        }
    }

    @FXML
    public void onClick(ActionEvent event) {
        if (isOtherTaskRunning()) {
            return;
        }
        switch (((MenuItem) event.getSource()).getId()) {
            case "menu_filter":
                if (!videoService.isVideoLoaded()) {
                    Toast.makeToast(stage, resourceBundle.getString("snackbar.empty.video"));
                    return;
                }
                fxUtil.openBlockStage(LayoutName.LAYOUT_FILTER, "main.caption.filter");
                break;
            case "menu_bat":
                if (appHolder.getCount() != 0 || !isNullOrEmpty(text_area.getText())) {
                    Optional<ButtonType> result = fxUtil.alertWithCancel(stage,
                            resourceBundle.getString("alert.title.user.warning"),
                            null,
                            resourceBundle.getString("alert.content.bat"));
                    if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                        return;
                    }
                }
                fxUtil.openBlockStage(LayoutName.LAYOUT_BAT, "main.file.bat");
                break;
            case "menu_settings":
                fxUtil.openBlockStage(LayoutName.LAYOUT_SETTINGS, "main.file.settings");
                break;
        }
    }

    private void openCOCR() {
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                try {
                    return fileService.readCOCR(cocrFile);
                } catch (Throwable ignored) {
                    return 0;
                }
            }

            @Override
            public void onResult(Integer result) {
                if (result == null || result != 1) {
                    return;
                }
                assFile = null;
                clearMatNodeAndOCR();
                if (appHolder.getCount() != 0) {
                    nextPage();
                }
                text_area.setText(appHolder.getOcr());
                file_name.setText(cocrFile.getName());
                if (videoAsyncTask.isCancelled()) {
                    btn_start.setText(resourceBundle.getString("main.start"));
                }
                videoService.closeVideo();
            }
        });
        service.submit(commonAsyncTask);
    }

    private void openVideo(File videoFile) {
        commonAsyncTask = new AsyncTask().setTaskListen(new AsyncTask.TaskListen() {
            @Override
            public void onPreExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
            }

            @Override
            public void onPostExecute() {
                fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
            }

            @Override
            public Integer call() {
                return videoService.loadVideo(videoFile);
            }

            @Override
            public void onResult(Integer result) {
                if (result == 1) {
                    appHolder.setOcr("");
                    clearMatNodeAndOCR();
                    cocrFile = null;
                    assFile = null;
                    btn_start.setText(resourceBundle.getString("main.start"));
                    file_name.setText(videoFile.getName());
                }
            }
        });
        service.submit(commonAsyncTask);
    }

    private void onBackgroundModify() {
        BigDecimal opacity = divide(BACKGROUND_OPACITY.intValue(), 100);
        mask.setStyle(String.format(DARK_THEME.booleanValue() ? MASK_DARK_STYLE : MASK_LIGHT_STYLE, opacity));
        menu_bar.setStyle(String.format(DARK_THEME.booleanValue() ? MASK_DARK_STYLE : MASK_LIGHT_STYLE, opacity));
        if (isNullOrEmpty(BACKGROUND_IMAGE.stringValue())) {
            root.setBackground(Background.EMPTY);
            return;
        }
        File file = new File(BACKGROUND_IMAGE.stringValue());
        try (FileInputStream fis = new FileInputStream(file)) {
            Image image = new Image(fis, 1920, 0, true, true);
            BackgroundImage backgroundImage = new BackgroundImage(image, NO_REPEAT, NO_REPEAT, CENTER,
                    new BackgroundSize(100, 100, true, true, true, true));
            root.setBackground(new Background(backgroundImage));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void onTextAreaPositionModify(ObservableValue<?> ov, Number a, Number b) {
        if (a.equals(b)) {
            return;
        }
        int line = matNodeService.getCaretPosition(text_area.getText(), text_area.getCaretPosition());
        if (line == oldLine) {
            return;
        }
        oldLine = line;
        selectItem(line - 1);
    }

    private void onTessLanguageModify(ObservableValue<?> ov, Number a, Number b) {
        service.submit(commonAsyncTask
                .setTaskListen(new AsyncTask.TaskListen() {
                    @Override
                    public void onPreExecute() {
                        fxUtil.onFXThread(progress_bar.progressProperty(), -1D);
                    }

                    @Override
                    public void onPostExecute() {
                        fxUtil.onFXThread(progress_bar.progressProperty(), 0D);
                    }

                    @Override
                    public Integer call() {
                        try {
                            ocrService.apiInit();
                            return 1;
                        } catch (Throwable ignored) {
                            return 0;
                        }
                    }

                    @Override
                    public void onResult(Integer result) {
                    }
                }));
    }

    private void onGroupItemSelected(ObservableValue<?> ov, Toggle a, Toggle b) {
        if (b == null) {
            a.setSelected(true);
            return;
        }
        MatNode matNode = (MatNode) b;
        int index = appHolder.getMatNodeList().indexOf(matNode);
        appHolder.setMatNodeSelectedIndex(index);
        frame_time.setText(matNodeService.getMatNodeFormatterTime(matNode));
        log.debug("Selected MatNode id: {}", matNode.getNid());
    }

    private void onScrollPaneVModify(ObservableValue<?> ov, Number a, Number b) {
        if (b.doubleValue() > 0.9 && flow_pane.getChildren().size() < appHolder.getCount()) {
            nextPage();
        }
    }

    private void onSliderModify(ObservableValue<?> ov, Number a, Number b) {
        scrollPaneVolume = 0;
        scrollPaneWidth = b.doubleValue() * 2;
        for (MatNode matNode : appHolder.getMatNodeList()) {
            matNode.zoom(scrollPaneWidth);
        }
    }

    private void onManagerModify(ObservableValue<?> ov, Boolean a, Boolean b) {
        if (appHolder.getMatNodeList().isEmpty()) {
            return;
        }
        for (MatNode matNode : appHolder.getMatNodeList()) {
            matNode.switchModel(!b);
        }
    }

    private void onMatNodeClick(MouseEvent mouseEvent, MatNode matNode) {
        if (!check_manager.isSelected()) {
            return;
        }
        switch (mouseEvent.getButton()) {
            case PRIMARY:
                if (matNode.isMerge()) {
                    matNodeService.markSaveTag(matNode);
                } else {
                    matNodeService.markDeleteTag(matNode);
                }
                break;
            case SECONDARY:
                group.selectToggle(matNode);
                matNodeService.markMergeTag(matNode);
                break;
        }
    }

    private void removeBeginTag() {
        if (!check_manager.isSelected()) {
            return;
        }
        matNodeService.removeMergeBeginTag();
    }

    private void clearMatNodeAndOCR() {
        flow_pane.getChildren().clear();
        text_area.setText("");
        System.gc();
    }

    private void nextPage() {
        if (scrollPaneVolume == 0 || matNodeHeight == 0) {
            matNodeHeight = appHolder.getMatNodeList().get(0).getHeight();
            scrollPaneVolume = (int) (scroll_pane.getHeight() / matNodeHeight);
        }
        List<MatNode> matNodeList = appHolder.getMatNodeList()
                .stream()
                .skip(flow_pane.getChildren().size())
                .limit(COUNT_PRE_PAGE.intValue())
                .collect(Collectors.toList());
        for (MatNode matNode : matNodeList) {
            matNode.setOnMouseClicked(mouseEvent -> onMatNodeClick(mouseEvent, matNode));
            matNode.loadImage(openCVService.mat2Image(matNode.getMat(), COMPRESS_IMAGE.booleanValue()), scrollPaneWidth);
            matNode.setToggleGroup(group);
        }
        flow_pane.getChildren().addAll(matNodeList);
    }

    private void selectItem(int index) {
        if (flow_pane.getChildren().size() == 0) {
            return;
        }
        if (index >= appHolder.getCount()) {
            return;
        }
        while (index > (flow_pane.getChildren().size() - 1)) {
            nextPage();
        }
        Node node = flow_pane.getChildren().get(index);
        group.selectToggle((MatNode) node);
        ensureVisible(node);
    }

    private void ensureVisible(Node node) {
        double h = scroll_pane.getContent().getBoundsInLocal().getHeight();
        double y = (node.getBoundsInParent().getMaxY() +
                node.getBoundsInParent().getMinY()) / 2.0;
        double v = scroll_pane.getViewportBounds().getHeight();
        scroll_pane.setVvalue((y - 0.2 * v) / (h - v));
    }

    private boolean isOtherTaskRunning() {
        if (commonAsyncTask.isRunning() || videoAsyncTask.isRunning()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.wait"));
            return true;
        }
        return false;
    }

}
