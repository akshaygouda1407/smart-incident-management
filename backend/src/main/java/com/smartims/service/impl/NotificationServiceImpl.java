package com.smartims.service.impl;

import com.smartims.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSlaBreachAlert(Long issueId, String title, String priority) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@smartims.local"); // can be anything in Mailtrap
        message.setSubject("SLA Breach Alert");
        message.setText(
                "SLA has been breached!\n\n" +
                        "Issue ID: " + issueId + "\n" +
                        "Title: " + title + "\n" +
                        "Priority: " + priority
        );

        mailSender.send(message);
    }
}
