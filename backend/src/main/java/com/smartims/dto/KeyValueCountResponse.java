package com.smartims.dto;

import lombok.Getter;

@Getter
public class KeyValueCountResponse {

    private String key;
    private long count;

    public KeyValueCountResponse(String key, long count) {
        this.key = key;
        this.count = count;
    }

}
