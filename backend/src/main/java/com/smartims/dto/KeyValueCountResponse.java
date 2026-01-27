package com.smartims.dto;

public class KeyValueCountResponse {

    private String key;
    private long count;

    public KeyValueCountResponse(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public long getCount() {
        return count;
    }
}
