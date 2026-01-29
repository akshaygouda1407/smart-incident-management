package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.entity.ContactMessage;
import com.smartims.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/submit")
    public ApiResponse<?> submitContact(
            @RequestBody ContactMessage message
    ) {
        contactService.submit(message);
        return ApiResponse.success("Message received successfully");
    }
}
