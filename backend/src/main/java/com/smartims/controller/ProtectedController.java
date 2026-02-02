package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    @GetMapping("/secure")
    public ResponseEntity<ApiResponse<String>> secureEndpoint() {

        return ResponseUtil.success(
                HttpStatus.OK,
                "Authentication successful",
                "You are authenticated"
        );
    }
}
