package com.smartims.util;

import com.smartims.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    private ResponseUtil() {

    }

    public static <T> ResponseEntity<ApiResponse<T>> success(
            HttpStatus status,
            String message,
            T data
    ) {
        return ResponseEntity.status(status).body(
                new ApiResponse<>(
                        status.value(),
                        "SUCCESS",
                        message,
                        data,
                        true
                )
        );
    }

    public static ResponseEntity<ApiResponse<?>> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status).body(
                new ApiResponse<>(
                        status.value(),
                        "FAILED",
                        message,
                        null,
                        false
                )
        );
    }
}
