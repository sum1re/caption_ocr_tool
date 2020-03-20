package com.neo.caption.ocr.service.impl;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.pojo.ModuleNodeAttribute;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.pojo.VideoHolder;
import com.neo.caption.ocr.service.ModuleService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.util.FieldUtil;
import com.neo.caption.ocr.view.ModuleNode;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@Slf4j
public class ModuleServiceImpl implements ModuleService {

    private final Gson gson;
    private final StageBroadcast stageBroadcast;
    private final ResourceBundle resourceBundle;
    private final VideoHolder videoHolder;
    private final FieldUtil fieldUtil;
    private final Joiner joiner;

    private final static String formatName = "/module/%1$s_module.json";

    public ModuleServiceImpl(Gson gson, StageBroadcast stageBroadcast, ResourceBundle resourceBundle,
                             VideoHolder videoHolder, FieldUtil fieldUtil, @Qualifier("dot") Joiner joiner) {
        this.gson = gson;
        this.stageBroadcast = stageBroadcast;
        this.resourceBundle = resourceBundle;
        this.videoHolder = videoHolder;
        this.fieldUtil = fieldUtil;
        this.joiner = joiner;
    }

    @AopException
    private ModuleNodeAttribute[] readAttrJson(ModuleType moduleType) {
        try (InputStream is = getClass().getResourceAsStream(String.format(formatName, moduleType.toLowerCase()));
             InputStreamReader isr = new InputStreamReader(is);
             JsonReader jsonReader = new JsonReader(isr)) {
            return gson.fromJson(jsonReader, ModuleNodeAttribute[].class);
        } catch (Exception ignored) {
            return new ModuleNodeAttribute[0];
        }
    }

    @Override
    public int cvtInt(Object object) {
        switch (object.getClass().getSimpleName()) {
            case "Integer":
                return (Integer) object;
            case "Double":
                return ((Double) object).intValue();
            default:
                return 0;
        }
    }

    @Override
    public double cvtDouble(Object object) {
        switch (object.getClass().getSimpleName()) {
            case "Integer":
                return ((Integer) object).doubleValue();
            case "Double":
                return (Double) object;
            default:
                return 0;
        }
    }

    @Override
    public ModuleNode generate(int index, ModuleType moduleType, boolean enable) {
        ModuleStatus moduleStatus = new ModuleStatus();
        moduleStatus.setIndex(index)
                .setModuleType(moduleType)
                .setEnable(enable)
                .setAttrMap(new HashMap<>())
                .setParamMap(new HashMap<>());
        ModuleNodeAttribute[] attributes = readAttrJson(moduleStatus.getModuleType());
        for (ModuleNodeAttribute attr : attributes) {
            Double def = attr.getParamDefaultValue();
            switch (attr.getParamNodeType()) {
                case SPINNER:
                case CHECK_BOX:
                    moduleStatus.getParamMap().put(attr.getParamTag(), def);
                    break;
                case CHOICE_BOX:
                    moduleStatus.getAttrMap().put(attr.getParamTag(), def);
                    moduleStatus.getParamMap().put(attr.getParamTag(),
                            fieldUtil.reflectAttrChoiceBox(attr.getParamOptions()[def.intValue()]));
                    break;
            }
        }
        return generate(moduleStatus, attributes);
    }

    @Override
    public ModuleNode generate(ModuleStatus moduleStatus) {
        ModuleNodeAttribute[] attributes = readAttrJson(moduleStatus.getModuleType());
        return generate(moduleStatus, attributes);
    }

    private ModuleNode generate(ModuleStatus moduleStatus, ModuleNodeAttribute[] attributes) {
        String nodeHead = joiner.join("module", moduleStatus.getModuleType().toLowerCase());
        ModuleNode moduleNode = new ModuleNode(moduleStatus, nodeHead, resourceBundle);
        List<Node> nodeList = new ArrayList<>();
        for (ModuleNodeAttribute attribute : attributes) {
            switch (attribute.getParamNodeType()) {
                case SPINNER:
                    generateSpinner(nodeHead, moduleStatus, attribute, nodeList);
                    break;
                case CHOICE_BOX:
                    generateChoiceBox(nodeHead, moduleStatus, attribute, nodeList);
                    break;
                case CHECK_BOX:
                    generateCheckBox(nodeHead, moduleStatus, attribute, nodeList);
                    break;
                default:
                    break;
            }
        }
        moduleNode.setModuleParam(nodeList);
        return moduleNode;
    }

    private void addTooltip(List<Node> nodeList, Control node, String nodeHead, ModuleNodeAttribute attribute) {
        String key = joiner.join(nodeHead, attribute.getParamTag());
        String name = resourceBundle.getString(joiner.join(key, "name"));
        if (isNullOrEmpty(name)) {
            return;
        }
        if (node instanceof CheckBox) {
            ((CheckBox) node).setText(name);
        } else {
            nodeList.add(new Label(name));
        }
        String description = resourceBundle.getString(joiner.join(key, "description"));
        if (!isNullOrEmpty(description)) {
            // set tooltip to node
            Tooltip.install(node, new Tooltip(description));
            // For spinner node, must manually access it's editor and set the tooltip.
            if (node instanceof Spinner) {
                Tooltip.install(((Spinner<?>) node).getEditor(), new Tooltip(description));
            }
        }
        nodeList.add(node);
    }

    private void generateSpinner(String nodeHead, ModuleStatus moduleStatus,
                                 ModuleNodeAttribute attribute, List<Node> nodeList) {
        String tag = attribute.getParamTag();
        Double max;
        switch (tag) {
            case "ulx":
            case "lrx":
                max = Double.valueOf(videoHolder.getWidth()) - 1;
                break;
            case "uly":
            case "lry":
                max = Double.valueOf(videoHolder.getHeight()) - 1;
                break;
            default:
                max = attribute.getParamMaxValue();
                break;
        }
        double def = cvtDouble(moduleStatus.getParamMap().getOrDefault(tag, attribute.getParamDefaultValue()));
        Spinner<Double> spinner = new Spinner<>();
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                attribute.getParamMinValue(), max, def, attribute.getParamIncrement());
        spinner.setValueFactory(factory);
        if (attribute.getEditable()) {
            spinner.setEditable(true);
            spinner.getEditor().setOnKeyPressed(keyEvent -> {
                switch (keyEvent.getCode()) {
                    case UP:
                        spinner.increment();
                        break;
                    case DOWN:
                        spinner.decrement();
                        break;
                }
            });
        }
        spinner.valueProperty()
                .addListener((ov, a, b) -> {
                    // To broadcast when the value has been modified.
                    if (!b.equals(a)) {
                        // attrMap is not necessary for spinner
                        // moduleStatus.getAttrMap().put(tag, b);
                        moduleStatus.getParamMap().put(tag, b);
                        stageBroadcast.sendModuleBroadcast();
                    }
                });
        addTooltip(nodeList, spinner, nodeHead, attribute);
    }

    private void generateChoiceBox(String nodeHead, ModuleStatus moduleStatus,
                                   ModuleNodeAttribute attribute, List<Node> nodeList) {
        String tag = attribute.getParamTag();
        Double def = moduleStatus.getAttrMap().getOrDefault(tag, attribute.getParamDefaultValue());
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setItems(FXCollections.observableArrayList(attribute.getParamOptions()));
        choiceBox.getSelectionModel().select(def.intValue());
        choiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ov, a, b) -> {
                    // To broadcast when the value has been modified.
                    if (!b.equals(a)) {
                        moduleStatus.getAttrMap().put(tag, (double) choiceBox.getSelectionModel().getSelectedIndex());
                        moduleStatus.getParamMap().put(tag, fieldUtil.reflectAttrChoiceBox(b));
                        stageBroadcast.sendModuleBroadcast();
                    }
                });
        addTooltip(nodeList, choiceBox, nodeHead, attribute);
    }

    private void generateCheckBox(String nodeHead, ModuleStatus moduleStatus,
                                  ModuleNodeAttribute attribute, List<Node> nodeList) {
        String tag = attribute.getParamTag();
        Double def = cvtDouble(moduleStatus.getParamMap().getOrDefault(tag, attribute.getParamDefaultValue()));
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(def.equals(1D));
        checkBox.selectedProperty()
                .addListener((ov, a, b) -> {
                    // To broadcast when the value has been modified.
                    // moduleStatus.getAttrMap().put(tag, b ? 1D : 0D);
                    moduleStatus.getParamMap().put(tag, b ? 1D : 0D);
                    stageBroadcast.sendModuleBroadcast();
                });
        addTooltip(nodeList, checkBox, nodeHead, attribute);
    }
}
