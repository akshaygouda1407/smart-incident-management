package com.smartims.exception;

import com.smartims.dto.ApiResponse;
import com.smartims.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 AUTHENTICATION ERROR (LOGIN / JWT / OTP)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuth(AuthenticationException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.UNAUTHORIZED,
                "AUTH ERROR → " + ex.getMessage()
        );
    }

    // 🔴 AUTHORIZATION ERROR (ROLE / PERMISSION)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccess(AccessDeniedException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.FORBIDDEN,
                "ACCESS DENIED → " + ex.getMessage()
        );
    }

    // 🟡 BUSINESS ERRORS
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    // 🔴 REAL FALLBACK (DO NOT HIDE)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handle(Exception ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getName() + " → " + ex.getMessage()
        );
    }
}
