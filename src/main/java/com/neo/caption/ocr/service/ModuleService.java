package com.neo.caption.ocr.service;

import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.view.ModuleNode;

public interface ModuleService {

    int cvtInt(Object object);

    double cvtDouble(Object object);

    ModuleNode generate(int index, ModuleType moduleType, boolean enable);

    ModuleNode generate(ModuleStatus moduleStatus);

}
