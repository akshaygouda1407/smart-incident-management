package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    @GetMapping("/secure")
    public ApiResponse<String> secureEndpoint() {

        return ApiResponse.success(
                "Authentication successful",
                "You are authenticated"
        );
    }
}
