package com.neo.caption.ocr.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;

import java.net.URL;
import java.util.ResourceBundle;

import static com.neo.caption.ocr.constant.LayoutName.LAYOUT_MAT_NODE;
import static com.neo.caption.ocr.util.BaseUtil.fxmlURL;

@Getter
@Setter
@Slf4j
public class MatNode extends ToggleButton implements Initializable {

    @FXML
    public Label delTag;
    @FXML
    public Label statusTag;
    @FXML
    public ImageView iv;

    private final int nid;
    private double startTime;
    private double endTime;
    private transient int mergeGroup;
    private transient boolean delete;
    private transient boolean merge;
    private transient boolean save;
    private final Mat mat;

    private MatNode(final int nid, final double startTime, final double endTime,
                    final int mergeGroup, final boolean delete, final boolean merge,
                    final boolean save, final Mat mat) {
        this.nid = nid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mergeGroup = mergeGroup;
        this.delete = delete;
        this.merge = merge;
        this.save = save;
        this.mat = mat;
        try {
            FXMLLoader loader = new FXMLLoader(fxmlURL(LAYOUT_MAT_NODE));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public MatNode(final int nid, final int startTime, final int endTime, final int mergeGroup, final Mat mat) {
        this(nid, startTime, endTime, mergeGroup, false, true, false, mat);
    }

    public MatNode(final int nid, final double startTime, final double endTime, final Mat mat) {
        this(nid, startTime, endTime, -1, false, false, false, mat);
    }

    public MatNode(final int nid, final double startTime, final Mat mat) {
        this(nid, startTime, startTime, mat);
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public final void loadImage(Image image, final double width) {
        iv.setFitWidth(width);
        iv.setImage(image);
    }

    public final void compress() {
        iv.setImage(new Image(iv.getImage().getUrl(), iv.getImage().getWidth() / 2D, iv.getImage().getHeight() / 2D, true, false));
    }

    private void modifyDelTag(final boolean b) {
        delTag.setVisible(b);
        this.delete = b;
    }

    public final void markDelete() {
        modifyDelTag(true);
    }

    public final void removeDelete() {
        modifyDelTag(false);
    }

    private void modifyStatusTag(final boolean b) {
        statusTag.setVisible(b);
        this.merge = b;
    }

    public final void markMerge(final String status) {
        modifyStatusTag(true);
        statusTag.setText(status);
    }

    public final void removeMerge() {
        modifyStatusTag(false);
        mergeGroup = -1;
    }

    private void modifySaveTag(boolean b) {
        statusTag.setText(b ? statusTag.getText() + "S" : statusTag.getText().substring(0, 1));
        this.save = b;
    }

    public final void markSave() {
        modifySaveTag(true);
    }

    public final void removeSave() {
        modifySaveTag(false);
    }

    public final void zoom(final double size) {
        iv.setFitWidth(size);
    }

    public final void switchModel(final boolean isEditModel) {
        if (isEditModel) {
            delTag.setVisible(false);
            statusTag.setVisible(false);
        } else {
            if (delete) {
                delTag.setVisible(true);
            }
            if (merge) {
                statusTag.setVisible(true);
            }
        }
    }

    public final boolean isImgLoaded() {
        return false;
    }

    @Override
    public final String toString() {
        return "MatNode{" +
                "nid=" + nid +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", mergeGroup=" + mergeGroup +
                ", delete=" + delete +
                ", merge=" + merge +
                ", save=" + save +
                '}';
    }
}
