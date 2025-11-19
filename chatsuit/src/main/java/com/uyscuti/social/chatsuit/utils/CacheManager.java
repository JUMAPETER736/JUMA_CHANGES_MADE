package com.uyscuti.social.chatsuit.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static final String CACHE_DIRECTORY = "file_size_cache";

    public static void cacheFileSize(Context context, String url, long fileSize) {
        try {
            Map<String, Long> cache = loadCache(context);

            if (cache == null) {
                cache = new HashMap<>();
            }

            cache.put(url, fileSize);

            saveCache(context, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getCachedFileSize(Context context, String url) {
        try {
            Map<String, Long> cache = loadCache(context);

            if (cache != null && cache.containsKey(url)) {
                return cache.get(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Return -1 if the URL is not in the cache
    }

    private static void saveCache(Context context, Map<String, Long> cache) throws Exception {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIRECTORY);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File cacheFile = new File(cacheDir, "file_size_cache.ser");

        try (FileOutputStream fileOut = new FileOutputStream(cacheFile);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(cache);
        }
    }

    private static Map<String, Long> loadCache(Context context) throws Exception {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIRECTORY);
        File cacheFile = new File(cacheDir, "file_size_cache.ser");

        if (!cacheFile.exists()) {
            return null;
        }

        try (FileInputStream fileIn = new FileInputStream(cacheFile);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (Map<String, Long>) in.readObject();
        }
    }
}
