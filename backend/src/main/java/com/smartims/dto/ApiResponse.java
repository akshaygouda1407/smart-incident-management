package com.smartims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private int statusCode;
    private String statusMessage;
    private String message;
    private T data;
    private boolean success;

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, "SUCCESS", message, null, true);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, "SUCCESS", message, data, true);
    }
}
