package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.entity.User;
import com.smartims.entity.UserNotification;
import com.smartims.repository.UserNotificationRepository;
import com.smartims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<UserNotification>> myNotifications(
            Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        return ApiResponse.success(
                "Notifications fetched successfully",
                userNotificationRepository
                        .findByUserOrderByReceivedAtDesc(user)
        );
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        return ApiResponse.success(
                "Unread notification count fetched successfully",
                userNotificationRepository
                        .countByUserAndReadFalse(user)
        );
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Object> markAsRead(@PathVariable Long id) {

        UserNotification notification =
                userNotificationRepository.findById(id)
                        .orElseThrow();

        notification.setRead(true);
        userNotificationRepository.save(notification);

        return ApiResponse.success(
                "Notification marked as read",
                null
        );
    }
}
