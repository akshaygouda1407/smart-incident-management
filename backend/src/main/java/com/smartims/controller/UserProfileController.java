package com.smartims.controller;

import com.smartims.dto.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserProfileController {

    @GetMapping("/api/me")
    public UserProfileResponse getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        return new UserProfileResponse(email, role);
    }
}
