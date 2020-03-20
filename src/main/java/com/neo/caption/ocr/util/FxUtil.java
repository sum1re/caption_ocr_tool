package com.neo.caption.ocr.util;

import com.neo.caption.ocr.constant.LayoutName;
import com.neo.caption.ocr.pojo.AppHolder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.neo.caption.ocr.AppPreloader.logoImage;
import static com.neo.caption.ocr.util.BaseUtil.fxmlURL;

@Slf4j
@Component
public class FxUtil {

    private final ApplicationContext context;
    private final ResourceBundle resourceBundle;
    private final AppHolder appHolder;
    private final PrefUtil prefUtil;

    public FxUtil(ApplicationContext context, ResourceBundle resourceBundle, AppHolder appHolder, PrefUtil prefUtil) {
        this.context = context;
        this.resourceBundle = resourceBundle;
        this.appHolder = appHolder;
        this.prefUtil = prefUtil;
    }

    public void loadStage(Stage stage, LayoutName layoutName, String titleKey) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL(layoutName), resourceBundle);
        fxmlLoader.setControllerFactory(context::getBean);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(resourceBundle.getString(titleKey));
        if (stage.getIcons().isEmpty()) {
            stage.getIcons().add(logoImage);
        }
        new JMetro(scene, prefUtil.isDarkTheme() ? Style.DARK : Style.LIGHT);
    }

    public <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }

    public void onFXThread(final DoubleProperty property, final Double value) {
        Platform.runLater(() -> property.set(value));
    }

    public void onFXThread(final StringProperty stringProperty, final String text) {
        Platform.runLater(() -> stringProperty.set(text));
    }

    public void setFontSize(TextArea textArea, int value) {
        textArea.setStyle("-fx-font-size: " + value);
    }

    public Optional<ButtonType> showAlert(
            Stage stage, String title, String headerText, String contentText,
            List<String> expandList, ButtonType... buttonTypes) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getButtonTypes().setAll(buttonTypes);
        if (expandList != null && !expandList.isEmpty()) {
            StringBuilder stringBuilder = appHolder.getStringBuilder();
            for (String s : expandList) {
                stringBuilder.append(s).append(System.lineSeparator());
            }
            if (stringBuilder.length() != 0) {
                TextArea textArea = new TextArea();
                textArea.setWrapText(true);
                textArea.setEditable(false);
                textArea.setText(stringBuilder.toString());
                textArea.setStyle("-fx-background-color: transparent");
                alert.getDialogPane().setExpandableContent(textArea);
            }
        }
        alert.getDialogPane().setStyle("-fx-padding: 8");
        new JMetro(alert.getDialogPane().getScene(), prefUtil.isDarkTheme() ? Style.DARK : Style.LIGHT);
        return alert.showAndWait();
    }

    public Optional<ButtonType> alertWithCancel(
            Stage stage, String title, String headerText, String contentText, List<String> expandList) {
        return showAlert(stage, title, headerText, contentText, expandList, ButtonType.CANCEL, ButtonType.OK);
    }

    public Optional<ButtonType> alertWithCancel(
            Stage stage, String title, String headerText, String contentText) {
        return showAlert(stage, title, headerText, contentText, null, ButtonType.CANCEL, ButtonType.OK);
    }

    public Optional<ButtonType> alert(
            Stage stage, String title, String headerText, String contentText, List<String> expandList) {
        return showAlert(stage, title, headerText, contentText, expandList, ButtonType.OK);
    }

    public Optional<ButtonType> alert(
            Stage stage, String title, String headerText, String contentText) {
        return showAlert(stage, title, headerText, contentText, null, ButtonType.OK);
    }

    private void openStage(Modality modality, LayoutName layoutName, String titleKey) {
        try {
            Stage stage = new Stage();
            stage.initModality(modality);
            loadStage(stage, layoutName, titleKey);
            stage.showAndWait();
        } catch (IOException impossible) {
            impossible.printStackTrace();
            log.debug("Fxml file {} has an IOException: {}", layoutName.getName(), impossible.getMessage());
        }
    }

    public void openBlockStage(LayoutName layoutName, String titleKey) {
        openStage(Modality.APPLICATION_MODAL, layoutName, titleKey);
    }

    public void openNoBlockStage(LayoutName layoutName, String titleKey) {
        openStage(Modality.NONE, layoutName, titleKey);
    }
}
