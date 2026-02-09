package com.example.caching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheInspectionService {

    @Autowired
    private CacheManager cacheManager;

    public void checkCacheContents() {
        Cache cache = cacheManager.getCache("weather");
        if (cache != null) {
            System.out.println(cache.getNativeCache());
        }
    }
}
