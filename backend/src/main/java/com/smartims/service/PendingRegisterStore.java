package com.smartims.service;

import com.smartims.dto.PendingRegisterUser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingRegisterStore {

    private final Map<String, PendingRegisterUser> store = new ConcurrentHashMap<>();

    public void save(String email, PendingRegisterUser user) {
        store.put(email, user);
    }

    public PendingRegisterUser get(String email) {
        return store.get(email);
    }

    public void remove(String email) {
        store.remove(email);
    }
}
