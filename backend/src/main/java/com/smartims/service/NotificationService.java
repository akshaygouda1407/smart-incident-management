package com.smartims.service;

public interface NotificationService {
    void sendSlaBreachAlert(Long issueId, String title, String priority);
}
