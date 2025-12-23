package com.uyscuti.social.circuit.cache.objectcache;

import android.util.Log;

import java.io.ByteArrayOutputStream;

class CachedObject {
    private final int expiryTimeSeconds;
    private final int expiryTimestamp;
    private final int creationTimestamp;
    private final boolean softExpiry;
    private String payload;

    public CachedObject(ByteArrayOutputStream payload, int expiryTimeSeconds, boolean softExpiry) {
        this.expiryTimeSeconds = expiryTimeSeconds <= 0 ? -1 : expiryTimeSeconds;
        this.creationTimestamp = (int) (System.currentTimeMillis() / 1000L);
        this.expiryTimestamp = expiryTimeSeconds <= 0 ? -1 : this.creationTimestamp + this.expiryTimeSeconds;
//		this.payload = payload;
        this.softExpiry = softExpiry;
        Log.d("SoundPickerActivity", "Created cache object");

    }


    public CachedObject(String payload, int expiryTimeSeconds, boolean softExpiry) {
        this.expiryTimeSeconds = expiryTimeSeconds <= 0 ? -1 : expiryTimeSeconds;
        this.creationTimestamp = (int) (System.currentTimeMillis() / 1000L);
        this.expiryTimestamp = expiryTimeSeconds <= 0 ? -1 : this.creationTimestamp + this.expiryTimeSeconds;
        this.payload = payload;
        this.softExpiry = softExpiry;
        Log.d("SoundPickerActivity", "Created cache object in cached object constructor");

    }

    public boolean isExpired() {
        return expiryTimeSeconds >= 0 && expiryTimestamp < (int) (System.currentTimeMillis() / 1000L);
    }

    public String getPayload() {
        return payload;
    }



    public boolean isSoftExpired() {
        return isExpired() && softExpiry;
    }
}
