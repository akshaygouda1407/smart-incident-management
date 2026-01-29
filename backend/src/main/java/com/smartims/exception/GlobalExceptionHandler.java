package com.smartims.exception;

import com.smartims.dto.ApiResponse;
import com.smartims.service.OtpException;
import com.smartims.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuth(AuthenticationException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.UNAUTHORIZED,
                "AUTH ERROR → " + ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccess(AccessDeniedException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.FORBIDDEN,
                "ACCESS DENIED → " + ex.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handle(Exception ex) {
        ex.printStackTrace();
        return ResponseUtil.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getName() + " → " + ex.getMessage()
        );
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthException(AuthException ex) {
        return ResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ApiResponse<?>> handleOtpException(OtpException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(
                        400,
                        "FAILED",
                        ex.getMessage(),
                        null,
                        false
                ));
    }
}
