package com.neo.caption.ocr.controller;

import com.google.common.base.Joiner;
import com.neo.caption.ocr.constant.ModuleProfile;
import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.service.StageService;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.pojo.VideoHolder;
import com.neo.caption.ocr.service.ModuleService;
import com.neo.caption.ocr.service.OpenCVService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.FieldUtil;
import com.neo.caption.ocr.util.PrefUtil;
import com.neo.caption.ocr.view.ModuleNode;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

import static com.neo.caption.ocr.constant.ModuleType.CROP;
import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.BaseUtil.v2s;
import static javafx.scene.input.TransferMode.MOVE;

@Controller
@Slf4j
@Lazy
public class FilterController implements BaseController {

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

    private final StageService stageService;
    private final VideoService videoService;
    private final OpenCVService openCVService;
    private final ModuleService moduleService;
    private final ResourceBundle resourceBundle;
    private final StageBroadcast stageBroadcast;
    private final VideoHolder videoHolder;
    private final FxUtil fxUtil;
    private final FieldUtil fieldUtil;
    private final PrefUtil prefUtil;
    private final Joiner joiner;

    private Stage stage;

    private Mat mat;
    private double frameCount;
    private boolean reorder;

    public FilterController(ResourceBundle resourceBundle, StageService stageService, VideoService videoService,
                            OpenCVService openCVService, ModuleService moduleService, StageBroadcast stageBroadcast,
                            VideoHolder videoHolder, FxUtil fxUtil, FieldUtil fieldUtil,
                            PrefUtil prefUtil, @Qualifier("dot") Joiner joiner) {
        this.resourceBundle = resourceBundle;
        this.stageService = stageService;
        this.videoService = videoService;
        this.openCVService = openCVService;
        this.moduleService = moduleService;
        this.stageBroadcast = stageBroadcast;
        this.videoHolder = videoHolder;
        this.fxUtil = fxUtil;
        this.fieldUtil = fieldUtil;
        this.prefUtil = prefUtil;
        this.joiner = joiner;
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
        reorder = false;
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
        choice_profile.getSelectionModel().select(prefUtil.getModuleProfileIndex());
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
    }

    @FXML
    public void onResetProfile() {
        Optional<ButtonType> result = fxUtil.alertWithCancel(stage,
                resourceBundle.getString("alert.title.user.warning"),
                null,
                resourceBundle.getString("alert.content.module.rest"));
        if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
            return;
        }
        PrefKey key;
        switch (ModuleProfile.values()[prefUtil.getModuleProfileIndex()]) {
            case HLS_COLOR:
                key = MODULE_PROFILE_HLS_COLOR;
                break;
            case HSV_COLOR:
                key = MODULE_PROFILE_HSV_COLOR;
                break;
            case FIXED_BINARY:
                key = MODULE_PROFILE_FIXED_BINARY;
                break;
            case ADAPTIVE_BINARY:
                key = MODULE_PROFILE_ADAPTIVE_BINARY;
                break;
            case CUSTOMIZE:
            default:
                key = MODULE_PROFILE_CUSTOMIZE;
                break;
        }
        prefUtil.removePref(key);
        prefUtil.loadModuleList();
        loadModuleStatus();
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
        int index = choice_profile.getSelectionModel().getSelectedIndex();
        if (index == prefUtil.getModuleProfileIndex()) {
            return;
        }
        prefUtil.setModuleProfileIndex(index);
        prefUtil.loadModuleList();
        loadModuleStatus();
    }

    private void onModuleNodeListModify(ListChangeListener.Change<?> change) {
        // Reorder if necessary.
        if (reorder) {
            int i = 0;
            for (Node node : module_node_list.getChildren()) {
                ((ModuleNode) node).setIndex(i);
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
        prefUtil.setModuleStatusList(module_node_list.getChildren()
                .stream()
                .map(node -> ((ModuleNode) node).getModuleStatus())
                .collect(Collectors.toList()));
        readVideo(frameCount);
    }

    private void loadModuleStatus() {
        //reset all ModuleNode, do not use 'getChildren().clear()'
        module_node_list.getChildren().setAll(prefUtil.getModuleStatusList()
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
        moduleNode.setDelAction(actionEvent -> module_node_list.getChildren().remove(moduleNode))
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
                        module_node_list.getChildren().add(index + 1, node);
                        reorder = true;
                        success = true;
                    }
                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });
    }
}
