package com.neo.caption.ocr.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.pojo.VideoHolder;
import com.neo.caption.ocr.service.*;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.FieldUtil;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.view.ModuleNode;
import com.neo.caption.ocr.view.Toast;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.neo.caption.ocr.constant.ModuleType.CROP;
import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.BaseUtil.v2s;
import static javafx.scene.input.TransferMode.MOVE;

@Controller
@Slf4j
@Lazy
public class FilterController implements BaseController {

    private final StageService stageService;
    private final VideoService videoService;
    private final OpenCVService openCVService;
    private final ModuleService moduleService;
    private final ResourceBundle resourceBundle;
    private final StageBroadcast stageBroadcast;
    private final VideoHolder videoHolder;
    private final FxUtil fxUtil;
    private final FieldUtil fieldUtil;
    private final PreferencesService preferencesService;
    private final Joiner joiner;
    private final FileService fileService;
    private final AppHolder appHolder;
    @FXML
    public SplitPane split_main;
    @FXML
    public Slider slider_zoom;
    @FXML
    public Slider slider_video;
    @FXML
    public CheckBox check_filter;
    @FXML
    public ChoiceBox<String> choice_profile;
    @FXML
    public VBox module_type_list;
    @FXML
    public VBox module_node_list;
    @FXML
    public ImageView image_view;
    private Stage stage;

    private Mat mat;
    private double frameCount;
    private boolean reorder;
    private boolean skipSave;

    public FilterController(PreferencesService preferencesService, StageService stageService, VideoService videoService,
                            OpenCVService openCVService, ModuleService moduleService, StageBroadcast stageBroadcast,
                            VideoHolder videoHolder, FxUtil fxUtil, FieldUtil fieldUtil, FileService fileService,
                            ResourceBundle resourceBundle, @Qualifier("dot") Joiner joiner, AppHolder appHolder) {
        this.resourceBundle = resourceBundle;
        this.stageService = stageService;
        this.videoService = videoService;
        this.openCVService = openCVService;
        this.moduleService = moduleService;
        this.stageBroadcast = stageBroadcast;
        this.videoHolder = videoHolder;
        this.fxUtil = fxUtil;
        this.fieldUtil = fieldUtil;
        this.preferencesService = preferencesService;
        this.joiner = joiner;
        this.fileService = fileService;
        this.appHolder = appHolder;
    }

    @Override
    public void init() {
        mat = new Mat();
        readVideo(0);
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            stageService.remove(stage);
            System.gc();
        });
    }

    @Override
    public void delay() {
        this.stage = stageService.add(split_main);
        setChoiceList();
        reorder = false;
        skipSave = false;
        // add all module
        module_type_list.getChildren().addAll(Arrays.stream(ModuleType.values())
                .parallel()
                .map(moduleType -> {
                    Button button = new Button();
                    button.setFocusTraversable(false);
                    button.setId(moduleType.name());
                    button.setOnAction(FilterController.this::onModuleTypeClick);
                    button.setText(resourceBundle.getString(joiner.join("module", moduleType.toLowerCase())));
                    return button;
                })
                .collect(Collectors.toList()));
        loadModuleStatus();
    }

    @Override
    public void bindListener() {
        slider_video.valueProperty()
                .addListener((ov, a, b) -> readVideo(b.doubleValue()));
        slider_zoom.valueProperty()
                .addListener((ov, a, b) -> image_view.setFitWidth(b.doubleValue() * 2));
        check_filter.selectedProperty()
                .addListener((ov, a, b) -> readVideo(frameCount));
        choice_profile.getSelectionModel()
                .selectedItemProperty()
                .addListener(this::onProfileBoxModify);
        module_node_list.getChildren()
                .addListener(this::onModuleNodeListModify);
        stageBroadcast.moduleBroadcast()
                .addListener((ov, a, b) -> updateFilter());
        stageBroadcast.profileListBroadcast()
                .addListener((ov, a, b) -> setChoiceList());
    }

    @FXML
    public void onCreate() throws IOException {
        String str = askInput(resourceBundle.getString("alert.content.module.profile.new"));
        if (str == null) {
            return;
        }
        fileService.saveModuleProfile(str, MODULE_PROFILE_DEFAULT.value());
        choice_profile.getSelectionModel().select(str);
    }

    @FXML
    public void onCopy() throws IOException {
        String str = askInput(resourceBundle.getString("alert.content.module.profile.copy"));
        if (str == null) {
            return;
        }
        fileService.saveModuleProfile(str, MODULE_PROFILE_STATUS_LIST.value());
        choice_profile.getSelectionModel().select(str);
    }

    @FXML
    public void onDelete() throws IOException {
        if (isNegative(resourceBundle.getString("alert.content.module.profile.delete"))) {
            return;
        }
        fileService.deleteModuleProfile(MODULE_PROFILE_NAME.stringValue());
        choice_profile.getSelectionModel().select(MODULE_PROFILE_NAME.stringValue());
    }

    @FXML
    public void onSliderScroll(ScrollEvent scrollEvent) {
        Slider slider = (Slider) scrollEvent.getSource();
        if (scrollEvent.getDeltaY() > 0 && slider.getValue() < slider.getMax()) {
            slider.valueProperty().set(slider.getValue() + slider.getBlockIncrement());
        } else if (scrollEvent.getDeltaY() < 0 && slider.getValue() > slider.getMin()) {
            slider.valueProperty().set(slider.getValue() - slider.getBlockIncrement());
        }
    }

    @FXML
    public void onMouseMovedOnImage(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        double axisX = (videoHolder.getWidth() * x) / (image_view.getFitWidth());
        double axisY = (videoHolder.getHeight() * y) / (image_view.getFitWidth() * videoHolder.getRatio());
        Scene scene = split_main.getScene();
        for (Map.Entry<String, Integer> entry : openCVService.getPixelColor((int) axisX, (int) axisY).entrySet()) {
            Label label = (Label) scene.lookup(entry.getKey());
            label.setText(v2s(entry.getValue()));
        }
    }

    private void onProfileBoxModify(ObservableValue<?> ov, String a, String b) {
        if (a == null || b == null || a.equals(b)) {
            return;
        }
        preferencesService.put(MODULE_PROFILE_NAME, b);
        try {
            fileService.loadModuleProfileStatusList();
        } catch (IOException ignored) {
        }
        skipSave = true;
        loadModuleStatus();
    }

    private void onModuleNodeListModify(ListChangeListener.Change<?> change) {
        // Reorder if necessary.
        if (reorder) {
            int i = 0;
            for (Node node : module_node_list.getChildren()) {
                ModuleNode moduleNode = (ModuleNode) node;
                if (moduleNode.getIndex() != i){
                    moduleNode.setIndex(i);
                }
                i++;
            }
            reorder = false;
        }
        updateFilter();
    }

    private void onModuleTypeClick(ActionEvent actionEvent) {
        String nodeFxId = ((Node) actionEvent.getSource()).getId();
        int index = module_node_list.getChildren().size();
        ModuleType type = fieldUtil.reflectFxId(nodeFxId);
        if (type != null) {
            ModuleNode moduleNode = moduleService.generate(index, type, true);
            setModuleNodeListener(moduleNode);
            module_node_list.getChildren().add(moduleNode);
        }
    }

    /**
     * Read one frame from the video.
     *
     * @param value frame count
     */
    private void readVideo(double value) {
        frameCount = value;
        if (!videoService.readFrame(mat, value / 100 * videoHolder.getTotalFrame())) {
            return;
        }
        openCVService.setVideoOriMat(mat);
        if (check_filter.isSelected()) {
            try {
                mat = openCVService.replaceRoiImage(mat);
            } catch (Throwable ignored) {
            }
        }
        Image toShow = openCVService.mat2Image(mat, false);
        fxUtil.onFXThread(image_view.imageProperty(), toShow);
    }

    private void updateFilter() {
        MODULE_PROFILE_STATUS_LIST.setValue(module_node_list.getChildren()
                .stream()
                .map(node -> ((ModuleNode) node).getModuleStatus())
                .collect(Collectors.toList()));
        if (skipSave) {
            skipSave = false;
            return;
        }
        try {
            fileService.saveModuleProfile(MODULE_PROFILE_NAME.stringValue(), MODULE_PROFILE_STATUS_LIST.value());
        } catch (IOException ignored) {
        }
        readVideo(frameCount);
    }

    @SuppressWarnings("unchecked")
    private void loadModuleStatus() {
        //reset all ModuleNode, do not use 'getChildren().clear()'
        module_node_list.getChildren().setAll(((List<ModuleStatus>) MODULE_PROFILE_STATUS_LIST.value())
                .stream()
                .map(moduleStatus -> {
                    ModuleNode moduleNode = moduleService.generate(moduleStatus);
                    // 'CROP' must be the first module, and can not modify.
                    if (moduleNode.getIndex() == 0 && moduleNode.getModuleStatus().getModuleType() == CROP) {
                        moduleNode.getCheck_enable().setDisable(true);
                        moduleNode.getBtn_del().setDisable(true);
                        return moduleNode;
                    }
                    setModuleNodeListener(moduleNode);
                    return moduleNode;
                })
                .collect(Collectors.toList()));

    }

    private void setModuleNodeListener(ModuleNode moduleNode) {
        moduleNode.setDelAction(actionEvent -> {
            reorder = true;
            module_node_list.getChildren().remove(moduleNode);
        })
                .setCacheListener((ov, a, b) -> {
                    moduleNode.getModuleStatus().setCache(b);
                    updateFilter();
                })
                .setEnableListener((ov, a, b) -> {
                    moduleNode.getModuleStatus().setEnable(b);
                    if (b) {
                        moduleNode.setOpacity(1.0);
                    } else {
                        moduleNode.setOpacity(0.5);
                    }
                    updateFilter();
                })
                .setDragDetected(mouseEvent -> {
                    Dragboard dragboard = moduleNode.startDragAndDrop(MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(UUID.randomUUID().toString());
                    dragboard.setContent(content);
                    dragboard.setDragView(moduleNode.snapshot(null, null));
                    mouseEvent.consume();
                })
                .setDragOver(dragEvent -> {
                    if (dragEvent.getDragboard().hasString() &&
                            dragEvent.getGestureSource() != moduleNode) {
                        dragEvent.acceptTransferModes(MOVE);
                    }
                    dragEvent.consume();
                })
                .setDragDropped(dragEvent -> {
                    Dragboard dragboard = dragEvent.getDragboard();
                    boolean success = false;
                    if (dragboard.hasString()) {
                        ModuleNode node = (ModuleNode) dragEvent.getGestureSource();
                        module_node_list.getChildren().remove(node);
                        int index = module_node_list.getChildren().indexOf(moduleNode);
                        reorder = true;
                        module_node_list.getChildren().add(index + 1, node);
                        success = true;
                    }
                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });
    }

    private void setChoiceList() {
        choice_profile.getItems().setAll(appHolder.getModuleProfileList());
        choice_profile.getSelectionModel().select(MODULE_PROFILE_NAME.stringValue());
    }

    private boolean isNegative(String context) {
        Optional<ButtonType> result = fxUtil.alertWithCancel(stage, resourceBundle.getString("alert.title.user.warning"), null, context);
        return result.isEmpty() || result.get() == ButtonType.CANCEL;
    }

    private String askInput(String contextText) {
        Optional<String> result = fxUtil.textInputAlert(stage, contextText);
        if (result.isEmpty()) {
            return null;
        }
        String str = result.get();
        if (Strings.isNullOrEmpty(str)) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.module.profile.invalid"));
            return null;
        }
        if (appHolder.getModuleProfileList().contains(str)) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.module.profile.exists"));
            return null;
        }
        return str;
    }
}
