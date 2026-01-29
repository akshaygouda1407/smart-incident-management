package com.smartims.util;

import com.smartims.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message, T data) {

        return ResponseEntity.ok(
                ApiResponse.success(message, data)
        );
    }

    public static ResponseEntity<ApiResponse<?>> error(
            HttpStatus status, String message) {

        return new ResponseEntity<>(
                new ApiResponse<>(
                        status.value(),
                        "FAILED",
                        message,
                        null,
                        false
                ),
                status
        );
    }
}
