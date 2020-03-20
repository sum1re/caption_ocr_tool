package com.neo.caption.ocr.view;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Toast {

    public static final String TOAST_STYLE = "-fx-background-color: rgba(0,0,0,.6);-fx-text-fill: #fff;-fx-background-radius: 20;-fx-border-width: 0;-fx-padding: 8 16";

    public static void makeToast(Stage ownerStage, String message) {
        makeToast(ownerStage, message, 2.5);
    }

    /**
     * Show a toast.
     *
     * @param ownerStage The stage that needs to show a toast.
     * @param message    The message that needs to be displayed.
     * @param delay      Show time in seconds.
     */
    public static void makeToast(Stage ownerStage, String message, double delay) {
        Tooltip toast = new Tooltip(message);
        toast.setStyle(TOAST_STYLE);
        toast.show(ownerStage);
        toast.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - toast.getWidth() / 2);
        toast.setY(ownerStage.getY() + ownerStage.getHeight() - toast.getHeight() * 2);

        Parent parent = toast.getScene().getRoot();
        // fade in
        FadeTransition fadeIn = fadeBuild(parent, 0.2, 0, 1);
        // fade out
        FadeTransition out = fadeBuild(parent, 0.5, 1, 0);
        out.setDelay(Duration.seconds(delay));
        out.setOnFinished(b -> toast.hide());
        // play animation
        SequentialTransition sequentialTransition = new SequentialTransition(fadeIn, out);
        sequentialTransition.play();
    }

    private static FadeTransition fadeBuild(Node node, double delay, double from, double to) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(delay), node);
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        return fadeTransition;
    }
}
