package com.neo.caption.ocr.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.MatNodeService;
import com.neo.caption.ocr.view.MatNode;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.util.BaseUtil.convertTime;

@Service
@Slf4j
public class MatNodeServiceImpl implements MatNodeService {

    private final AppHolder appHolder;
    private final Joiner joiner;
    private final Splitter splitter;

    private Map<Integer, List<MatNode>> mergeMap;
    private int mergeStart;
    private int mergeCount;

    public MatNodeServiceImpl(
            AppHolder appHolder, @Qualifier("arrow") Joiner joiner, @Qualifier("lineSeparator") Splitter splitter) {
        this.appHolder = appHolder;
        this.joiner = joiner;
        this.splitter = splitter;
    }

    @PostConstruct
    public void init() {
        this.mergeMap = new HashMap<>();
        this.mergeStart = -1;
        this.mergeCount = 0;
    }

    @Override
    public void handleDeleteAndMergeTag(FlowPane flowPane) {
        HashSet<MatNode> del = new HashSet<>(512);
        Iterator<Map.Entry<Integer, List<MatNode>>> iterator = mergeMap.entrySet().iterator();
        List<MatNode> removeTagList = new ArrayList<>(512);
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<MatNode>> entry = iterator.next();
            List<MatNode> tp = entry.getValue();
            double start = tp.get(0).getStartTime();
            double end = tp.get(tp.size() - 1).getEndTime();
            // update the list
            tp.stream()
                    .filter(MatNode::isSave)
                    .forEach(matNode -> {
                        int index = appHolder.getMatNodeList().indexOf(matNode);
                        matNode.setStartTime(start);
                        matNode.setEndTime(end);
                        appHolder.getMatNodeList().set(index, matNode);
                        removeTagList.add(matNode);
                    });
            // remove MatNode objects without the Save tag
            del.addAll(tp.stream()
                    .filter(matNode -> !matNode.isSave())
                    .collect(Collectors.toList()));
            tp.removeAll(del);
            // remove from the map
            iterator.remove();
        }
        // remove MatNode objects with the Del tag
        del.addAll(appHolder.getMatNodeList()
                .stream()
                .filter(MatNode::isDelete)
                .collect(Collectors.toList()));
        appHolder.getMatNodeList().removeAll(del);
        Platform.runLater(() -> {
            flowPane.getChildren().removeAll(del);
            for (MatNode matNode : removeTagList) {
                removeAllTag(matNode);
            }
        });
    }

    @Override
    public void markDeleteTag(MatNode matNode) {
        if (mergeStart != -1) {
            appHolder.getMatNodeList()
                    .get(mergeStart)
                    .removeMerge();
            if (appHolder.getMatNodeSelectedIndex() < mergeStart) {
                switchValue();
            }
            appHolder.getMatNodeList()
                    .stream()
                    .skip(mergeStart)
                    .limit(appHolder.getMatNodeSelectedIndex() - mergeStart + 1)
                    .forEach(matNode1 -> {
                        // remove MatNode objects without the Merge tag
                        if (!matNode1.isMerge()) {
                            matNode1.markDelete();
                        }
                    });
            mergeStart = -1;
            return;
        }
        // if it has Del tag, then remove the tag
        if (matNode.isDelete()) {
            matNode.removeDelete();
            return;
        }
        // if it has Save tag, it can not mark Del tag
        if (matNode.isSave()) {
            return;
        }
        matNode.markDelete();
    }

    @Override
    public void markMergeTag(MatNode matNode) {
        if (matNode.isMerge()) {
            int mg = matNode.getMergeGroup();
            if (mg == -1) {
                matNode.removeMerge();
                mergeStart = -1;
                return;
            }
            // cancel merge and remove all tags
            mergeMap.get(mg)
                    .forEach(this::removeAllTag);
            // remove the group from the map
            mergeMap.remove(mg);
            return;
        }
        if (mergeStart == -1) { // merge start
            if (matNode.isDelete()) {
                matNode.removeDelete();
            }
            matNode.markMerge("B");
            mergeStart = appHolder.getMatNodeSelectedIndex();
            return;
        }
        if (appHolder.getMatNodeSelectedIndex() < mergeStart) {
            switchValue();
        }
        List<MatNode> matNodeList = appHolder.getMatNodeList()
                .stream()
                .skip(mergeStart)
                .limit(appHolder.getMatNodeSelectedIndex() - mergeStart + 1)
                .collect(Collectors.toList());
        for (MatNode node : matNodeList) {
            if (node.getMergeGroup() != -1) {
                return;
            }
        }
        matNodeList.get(0).markMerge("B");
        matNodeList.get(matNodeList.size() - 1).markMerge("E");
        matNodeList.get(matNodeList.size() - 1).markSave();
        matNodeList.forEach(node -> {
            node.setMergeGroup(mergeCount);
            // remove MatNode objects with Del tag
            if (node.isDelete()) {
                node.removeDelete();
            }
            // exclude the first one and the last one
            if (!node.isMerge()) {
                node.markMerge("M");
            }
        });
        mergeStart = -1;
        // add Group to the map
        mergeMap.put(mergeCount, matNodeList);
        mergeCount += 1;
    }

    @Override
    public void markSaveTag(MatNode matNode) {
        if (!matNode.isMerge() || matNode.isSave()) {
            return;
        }
        int groupId = matNode.getMergeGroup();
        // if group id is -1, it means that it only marks the 'Begin' tag
        if (groupId == -1) {
            return;
        }
        List<MatNode> mns = mergeMap.get(groupId);
        for (MatNode matNode1 : mns) {
            // remove other Save tags in the group
            if (matNode1.isSave()) {
                matNode1.removeSave();
                break;
            }
        }
        // mark Save tag for dst MatNode
        matNode.markSave();
    }

    @Override
    public void removeAllTag(MatNode matNode) {
        if (matNode.isDelete()) {
            matNode.removeDelete();
        }
        if (matNode.isMerge()) {
            matNode.removeMerge();
        }
        if (matNode.isSave()) {
            matNode.removeSave();
        }
    }

    @Override
    public void removeMergeBeginTag() {
        if (mergeStart == -1) {
            return;
        }
        MatNode matNode = appHolder.getMatNodeList().get(mergeStart);
        matNode.removeMerge();
        mergeStart = -1;
    }

    /**
     * show the node's information
     *
     * @param matNode selected MatNode
     * @return start time -> end time
     */
    @Override
    public String getMatNodeFormatterTime(MatNode matNode) {
        return joiner.join(convertTime(matNode.getStartTime()),
                convertTime(matNode.getEndTime()));
    }

    @Override
    public Integer getCaretPosition(String string, int caretPosition) {
        if (isNullOrEmpty(string)) {
            return 0;
        }
        ImmutableList<String> list = ImmutableList.copyOf(splitter.split(string));
        int count = 0;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            count += list.get(i).length() + 1;
            if (count > caretPosition) {
                return i + 1;
            }
        }
        return len + 1;
    }

    /**
     * A = 1, B = 2 -> A = 2, B = 1
     */
    private void switchValue() {
        appHolder.setMatNodeSelectedIndex(appHolder.getMatNodeSelectedIndex() ^ mergeStart);
        mergeStart = appHolder.getMatNodeSelectedIndex() ^ mergeStart;
        appHolder.setMatNodeSelectedIndex(appHolder.getMatNodeSelectedIndex() ^ mergeStart);
    }
}
