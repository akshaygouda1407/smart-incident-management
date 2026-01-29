package com.smartims.controller;

import com.smartims.dto.*;
import com.smartims.enums.OtpPurpose;
import com.smartims.service.OtpService;
import com.smartims.service.UserService;
import com.smartims.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;


    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseUtil.success(
                "Login successful",
                userService.login(request)
        );
    }

    @PostMapping("/register/requestOtp")
    public ApiResponse<?> requestRegisterOtp(
            @RequestBody Map<String, String> body
    ) {
        otpService.generateAndSendOtp(body.get("email"), OtpPurpose.REGISTER);
        return ApiResponse.success("OTP sent to email");
    }


    @PostMapping("/register/verifyOtp")
    public ApiResponse<?> verifyRegisterOtp(
            @RequestBody Map<String, String> body
    ) {
        otpService.verifyOtp(
                body.get("email"),
                body.get("otp"),
                OtpPurpose.REGISTER
        );
        return ApiResponse.success("OTP verified");
    }

    @PostMapping("/forgotPassword/requestOtp")
    public ApiResponse<?> forgotPasswordOtp(@RequestBody Map<String, String> body) {
        otpService.generateAndSendOtp(body.get("email"), OtpPurpose.FORGOT_PASSWORD);
        return ApiResponse.success("OTP sent");
    }

    @PostMapping("/forgotPassword/verifyOtp")
    public ApiResponse<?> verifyForgotOtp(
            @RequestBody Map<String, String> body
    ) {
        otpService.verifyOtp(
                body.get("email"),
                body.get("otp"),
                OtpPurpose.FORGOT_PASSWORD
        );

        userService.resetPassword(
                body.get("email"),
                body.get("newPassword")
        );

        return ApiResponse.success("Password reset successful");
    }


}

