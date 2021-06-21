package com.neo.caption.ocr.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Files;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.OCRService;
import com.neo.caption.ocr.service.StageService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.AsyncTask;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.view.BatNode;
import com.neo.caption.ocr.view.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Controller
@Lazy
@Slf4j
public class BatController implements BaseController {

    @FXML
    public VBox root;
    @FXML
    public VBox bat_node_list;
    @FXML
    public Button btn_start;
    @FXML
    public Button btn_add;
    @FXML
    public CheckBox save_to_txt;

    private final StageService stageService;
    private final VideoService videoService;
    private final FileService fileService;
    private final OCRService ocrService;
    private final ResourceBundle resourceBundle;
    private final StageBroadcast stageBroadcast;
    private final Joiner joiner;
    private final AppHolder appHolder;
    private final FxUtil fxUtil;
    private final ExecutorService service;

    private Stage stage;
    private ToggleGroup group;
    private AsyncTask asyncTask;

    private boolean warning;
    private boolean skipOCR;

    public BatController(
            StageService stageService, VideoService videoService, FileService fileService, OCRService ocrService,
            ResourceBundle resourceBundle, StageBroadcast stageBroadcast, @Qualifier("dot") Joiner joiner,
            AppHolder appHolder, FxUtil fxUtil, ExecutorService service) {
        this.stageService = stageService;
        this.videoService = videoService;
        this.fileService = fileService;
        this.ocrService = ocrService;
        this.resourceBundle = resourceBundle;
        this.stageBroadcast = stageBroadcast;
        this.joiner = joiner;
        this.appHolder = appHolder;
        this.fxUtil = fxUtil;
        this.service = service;
    }

    @Override
    public void init() {
        this.group = new ToggleGroup();
        this.skipOCR = false;
        this.asyncTask = new AsyncTask();
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            appHolder.getMatNodeList().clear();
            appHolder.setOcr("");
            stageService.remove(stage);
            if (asyncTask.isRunning()) {
                asyncTask.cancel(true);
            }
            System.gc();
        });
    }

    @Override
    public void delay() {
        this.stage = stageService.add(root);
        this.warning = appHolder.getCount() != 0 || !isNullOrEmpty(appHolder.getOcr());
    }

    @Override
    public void bindListener() {

    }

    @FXML
    public void onClick() {
        if (bat_node_list.getChildren().isEmpty()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.empty.bat"));
            return;
        }
        if (warning) {
            Optional<ButtonType> result = fxUtil.alertWithCancel(stage,
                    resourceBundle.getString("alert.title.user.warning"),
                    null,
                    resourceBundle.getString("alert.content.bat.start"));
            if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                return;
            }
            stageBroadcast.sendDataEmptyBroadcast();
            warning = false;
        }
        if (asyncTask.isRunning()) {
            asyncTask.cancel(true);
            return;
        }
        UnmodifiableIterator<BatNode> unmodifiableIterator = bat_node_list.getChildren()
                .stream()
                .map(node -> (BatNode) node)
                .filter(node -> node.isValid() && !node.isFinish() && !node.isError())
                .collect(toImmutableList())
                .iterator();
        if (!unmodifiableIterator.hasNext()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.empty.bat"));
            return;
        }
        asyncTask = new AsyncTask()
                .setTaskListen(new AsyncTask.TaskListen() {

                    @Override
                    public void onPreExecute() {
                        fxUtil.onFXThread(btn_start.textProperty(), getBatBundle("stop"));
                    }

                    @Override
                    public void onPostExecute() {

                    }

                    @Override
                    public Integer call() {
                        while (unmodifiableIterator.hasNext() && !Thread.currentThread().isInterrupted()) {
                            BatNode batNode = unmodifiableIterator.next();
                            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.WORKING.toLowerCase()));
                            if (batNode.getFile().getName().endsWith(".cocr")) {
                                doOCRTask(batNode);
                            } else {
                                doVideoTask(batNode);
                            }
                        }
                        return 1;
                    }

                    @Override
                    public void cancelled() {
                        fxUtil.onFXThread(btn_start.textProperty(), getBatBundle("start"));
                    }

                    @Override
                    public void onResult(Integer result) {
                        if (result == 1) {
                            fxUtil.onFXThread(btn_start.textProperty(), getBatBundle("start"));
                        }
                    }
                });
        service.submit(asyncTask);
    }

    @FXML
    public void onAdd() {
        if (asyncTask.isRunning()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.wait"));
            return;
        }
        List<File> fileList = fileService.openMultiFileDialog(
                stage, resourceBundle.getString("file.choose.bat"));
        if (fileList == null || fileList.isEmpty()) {
            return;
        }
        bat_node_list.getChildren().addAll(fileList.stream()
                .map(file -> { BatNode batNode = new BatNode();
                    batNode.setValid(fileService.verifyBatFile(file))
                            .setFile(file)
                            .setDeleteAction(actionEvent -> onBatNodeDelete(batNode))
                            .setToggleGroup(group);
                    fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(
                            (batNode.isValid() ? BatStatus.READY : BatStatus.INVALID).toLowerCase()));
                    return batNode;
                })
                .collect(Collectors.toList()));
    }

    private void onBatNodeDelete(BatNode batNode) {
        if (asyncTask.isRunning()) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.wait"));
            return;
        }
        if (batNode == null) {
            return;
        }
        bat_node_list.getChildren().remove(batNode);
    }

    private void doVideoTask(BatNode batNode) {
        try {
            videoService.loadVideo(batNode.getFile());
            videoService.videoToCOCR(batNode.getProgress_bar());
            if (videoService.isVideoFinish()) {
                fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.SAVING.toLowerCase()));
                appHolder.setOcr("");
                fileService.saveCOCR(toDstFile(batNode.getFile(), "cocr"));
                batNode.setFinish(true);
            }
        } catch (Throwable throwable) {
            setErrorStatus(batNode);
        } finally {
            videoService.closeVideo();
            resetStatus(batNode);
        }
    }

    private void doOCRTask(BatNode batNode) {
        if (skipOCR) {
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.ERROR.toLowerCase()));
            return;
        }
        try {
            if (!ocrService.isReady()) {
                ocrService.apiInit();
                skipOCR = !ocrService.isReady();
            }
            fileService.readCOCR(batNode.getFile());
            if (ocrService.doOCR(batNode.getProgress_bar()) == 0) {
                return;
            }
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.SAVING.toLowerCase()));
            fileService.saveCOCR(toDstFile(batNode.getFile(), "cocr"));
            if (save_to_txt.isSelected()) {
                fileService.saveOCRText(toDstFile(batNode.getFile(), "txt"));
            }
            batNode.setFinish(true);
        } catch (Throwable throwable) {
            setErrorStatus(batNode);
        } finally {
            resetStatus(batNode);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private File toDstFile(File ori, String extension) {
        String oriName = Files.getNameWithoutExtension(ori.getName());
        return new File(ori.getParentFile(), joiner.join(oriName, extension));
    }

    private String getBatBundle(String key) {
        return resourceBundle.getString(joiner.join("bat", key));
    }

    private void resetStatus(BatNode batNode) {
        if (batNode.isError()) {
            return;
        }
        fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(
                (batNode.isFinish() ? BatStatus.DONE : BatStatus.READY).toLowerCase()));
    }

    private void setErrorStatus(BatNode batNode) {
        batNode.setError(true);
        fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.ERROR.toLowerCase()));
    }

    private enum BatStatus {

        INVALID,
        READY,
        WORKING,
        SAVING,
        DONE,
        ERROR;

        public String toLowerCase() {
            return this.name().toLowerCase();
        }

    }
}
