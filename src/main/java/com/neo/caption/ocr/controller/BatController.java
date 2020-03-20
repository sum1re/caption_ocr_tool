package com.neo.caption.ocr.controller;

import com.google.common.base.Joiner;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.OCRService;
import com.neo.caption.ocr.service.StageService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.view.BatNode;
import com.neo.caption.ocr.view.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Controller
@Lazy
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
    public Button btn_remove;

    private final StageService stageService;
    private final VideoService videoService;
    private final FileService fileService;
    private final OCRService ocrService;
    private final ResourceBundle resourceBundle;
    private final StageBroadcast stageBroadcast;
    private final Joiner joiner;
    private final AppHolder appHolder;
    private final FxUtil fxUtil;

    private Stage stage;
    private ToggleGroup group;
    private ExecutorService service;

    private boolean work;
    private boolean warning;
    private boolean skipOCR;

    public BatController(StageService stageService, VideoService videoService, FileService fileService,
                         OCRService ocrService, ResourceBundle resourceBundle, StageBroadcast stageBroadcast,
                         @Qualifier("dot") Joiner joiner, AppHolder appHolder, FxUtil fxUtil) {
        this.stageService = stageService;
        this.videoService = videoService;
        this.fileService = fileService;
        this.ocrService = ocrService;
        this.resourceBundle = resourceBundle;
        this.stageBroadcast = stageBroadcast;
        this.joiner = joiner;
        this.appHolder = appHolder;
        this.fxUtil = fxUtil;
    }

    @Override
    public void init() {
        this.group = new ToggleGroup();
        this.work = false;
        this.skipOCR = false;
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            if (service != null && !service.isShutdown()) {
                service.shutdownNow();
            }
            appHolder.getMatNodeList().clear();
            appHolder.setOcr("");
            stageService.remove(stage);
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
        if (service == null || service.isShutdown()) {
            service = new ThreadPoolExecutor(1, 1, 0L,
                    TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(1024));
        }
        if (work) {
            work = false;
            service.shutdownNow();
            btn_start.setText(getBatBundle("start"));
        } else {
            work = true;
            btn_start.setText(getBatBundle("stop"));
            Iterator<BatNode> iterator = bat_node_list.getChildren()
                    .stream()
                    .map(node -> (BatNode) node)
                    .filter(node -> node.isValid() && !node.isFinish() && !node.isError())
                    .iterator();
            while (iterator.hasNext()) {
                addBatTask(iterator.next(), iterator.hasNext());
            }
        }
    }

    @FXML
    public void onAdd() {
        if (work) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.wait"));
            return;
        }
        List<File> fileList = fileService.openMultiFileDialog(
                stage, resourceBundle.getString("file.choose.bat"));
        if (fileList == null || fileList.isEmpty()) {
            return;
        }
        bat_node_list.getChildren().addAll(fileList.stream()
                .map(file -> {
                    BatNode batNode = new BatNode();
                    batNode.setValid(fileService.verifyBatFile(file))
                            .setFile(file)
                            .setToggleGroup(group);
                    fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(
                            (batNode.isValid() ? BatStatus.READY : BatStatus.INVALID).toLowerCase()));
                    return batNode;
                })
                .collect(Collectors.toList()));
    }

    @FXML
    public void onRemove() {
        if (work) {
            return;
        }
        BatNode batNode = (BatNode) group.getSelectedToggle();
        if (batNode == null) {
            return;
        }
        bat_node_list.getChildren().remove(batNode);
    }

    private void addBatTask(BatNode batNode, boolean hasNext) {
        Runnable runnable = () -> {
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.WORKING.toLowerCase()));
            if (batNode.getFile().getName().endsWith(".cocr")) {
                doOCRTask(batNode);
            } else {
                doVideoTask(batNode);
            }
            work = false;
            if (!hasNext) {
                fxUtil.onFXThread(btn_start.textProperty(), getBatBundle("start"));
            }
        };
        service.execute(runnable);
    }

    private void doVideoTask(BatNode batNode) {
        try {
            videoService.loadVideo(batNode.getFile());
            videoService.videoToCOCR(batNode.getProgress_bar());
            if (videoService.isVideoFinish()) {
                fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.SAVING.toLowerCase()));
                appHolder.setOcr("");
                fileService.saveCOCR(toCOCRFile(batNode.getFile()));
                batNode.setFinish(true);
            }
        } catch (Throwable throwable) {
            batNode.setError(true);
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.ERROR.toLowerCase()));
        } finally {
            videoService.closeVideo();
            if (!batNode.isError()) {
                fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(
                        (batNode.isFinish() ? BatStatus.DONE : BatStatus.READY).toLowerCase()));
            }
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
            ocrService.doOCR(batNode.getProgress_bar());
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.SAVING.toLowerCase()));
            fileService.saveCOCR(toCOCRFile(batNode.getFile()));
            batNode.setFinish(true);
        } catch (Throwable throwable) {
            batNode.setError(true);
            fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(BatStatus.ERROR.toLowerCase()));
        } finally {
            if (!batNode.isError()) {
                fxUtil.onFXThread(batNode.statusProperty(), getBatBundle(
                        (batNode.isFinish() ? BatStatus.DONE : BatStatus.READY).toLowerCase()));
            }
        }
    }

    private File toCOCRFile(File ori) {
        String path = ori.getAbsolutePath();
        path = path.substring(0, path.lastIndexOf(".")) + ".cocr";
        return new File(path);
    }

    private String getBatBundle(String key) {
        return resourceBundle.getString(joiner.join("bat", key));
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
