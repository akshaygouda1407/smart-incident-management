package com.smartims.service.impl;

import com.smartims.dto.*;
import com.smartims.entity.User;
import com.smartims.enums.OtpPurpose;
import com.smartims.enums.Role;
import com.smartims.exception.AuthException;
import com.smartims.repository.UserRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.OtpService;
import com.smartims.service.PendingRegisterStore;
import com.smartims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.smartims.security.JwtService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PendingRegisterStore pendingRegisterStore;
    private final AuditLogService auditLogService;


    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!user.getEnabled() || user.getLocked()) {
            throw new AuthException("Account is disabled or locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, user.getRole());
    }


    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        PendingRegisterUser pending = new PendingRegisterUser();
        pending.setEmail(request.getEmail());
        pending.setFullName(request.getFullName());
        pending.setPassword(request.getPassword());
        pending.setRole(Role.ADMIN);

        pendingRegisterStore.save(request.getEmail(), pending);
    }


    @Override
    public void createUserFromPending(PendingRegisterUser pending) {

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setFullName(pending.getFullName());
        user.setPassword(passwordEncoder.encode(pending.getPassword()));
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        user.setVerified(true);
        user.setLocked(false);

        userRepository.save(user);
    }

    @Override
    public void enableUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        user.setVerified(true);

        userRepository.save(user);
    }

    @Override
    public void ensureUserExists(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("No account found with this email");
        }
    }

    @Override
    public void resetPassword(String email, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .locked(false)
                .build();

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());

        if (request.getRole() != null)
            user.setRole(request.getRole());

        if (request.getEnabled() != null)
            user.setEnabled(request.getEnabled());

        if (request.getLocked() != null)
            user.setLocked(request.getLocked());

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .build();
    }

    @Override
    public void updateUserStatus(Long userId, boolean enabled) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(enabled);
        userRepository.save(user);

        auditLogService.log(
                "UPDATE_USER_STATUS",
                "USER",
                user.getId(),
                enabled ? "User enabled" : "User disabled"
        );
    }

    @Override
    public void updateUserLockStatus(Long userId, boolean locked) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLocked(locked);
        userRepository.save(user);

        auditLogService.log(
                "UPDATE_USER_LOCK",
                "USER",
                user.getId(),
                locked ? "User account locked" : "User account unlocked"
        );
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        //Encode & update new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        //Invalidate existing tokens (VERY IMPORTANT)
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        //Audit log
        auditLogService.log(
                "CHANGE_PASSWORD",
                "USER",
                user.getId(),
                "User changed password"
        );
    }

}
