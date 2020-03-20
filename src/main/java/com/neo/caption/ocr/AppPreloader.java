package com.neo.caption.ocr;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.neo.caption.ocr.constant.LayoutName.LAYOUT_SPLASH;
import static com.neo.caption.ocr.util.BaseUtil.fxmlURL;
import static javafx.scene.layout.BackgroundPosition.CENTER;
import static javafx.scene.layout.BackgroundRepeat.NO_REPEAT;

@Slf4j
public class AppPreloader extends Preloader {

    private Stage stage;

    public static Image logoImage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Parent parent = new FXMLLoader(fxmlURL(LAYOUT_SPLASH)).load();
        Scene scene = new Scene(parent);

        try (InputStream splashInputStream = getSplashInputStream();
             InputStream logoInputStream = getClass().getResourceAsStream("/image/logo.png")) {
            logoImage = new Image(logoInputStream, 512, 512, true, true);
            Image image = new Image(splashInputStream, 1280, 0, true, true);
            AnchorPane ap = (AnchorPane) scene.lookup("#ap");
            BackgroundImage backgroundImage = new BackgroundImage(image, NO_REPEAT, NO_REPEAT, CENTER,
                    new BackgroundSize(100, 100, true, true, true, true));
            ap.setBackground(new Background(backgroundImage));
        }

        new JMetro(scene, Style.LIGHT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.getIcons().add(logoImage);
        stage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification preloaderNotification) {
        if (preloaderNotification instanceof StateChangeNotification) {
            stage.hide();
        }
    }

    private InputStream getSplashInputStream() throws IOException {
        File[] files = new File(System.getProperty("cocr.dir"), "../splash")
                .listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
        if (files == null || files.length == 0) {
            return getClass().getResourceAsStream("/image/splash.png");
        } else {
            return new FileInputStream(files[0]);
        }
    }
}
