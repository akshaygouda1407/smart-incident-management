package com.smartims.controller;

import com.smartims.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestEmailController {

    private final NotificationService notificationService;

    @GetMapping("/test-email")
    public String testEmail() {
        notificationService.sendSlaBreachAlert(
                999L,
                "Mailtrap Test Email",
                "P1"
        );
        return "Test email sent successfully";
    }
}
