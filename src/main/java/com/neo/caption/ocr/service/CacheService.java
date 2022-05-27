package com.neo.caption.ocr.service;

import com.neo.caption.ocr.constant.CacheKeyPrefix;

public interface CacheService {

    <T> T getCache(CacheKeyPrefix prefix, String key);

    <T> T putCache(CacheKeyPrefix prefix, String key, T object);

    void removeCache(CacheKeyPrefix prefix, String key);

}
