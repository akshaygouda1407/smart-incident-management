package com.smartims.service.impl;

import com.smartims.dto.*;
import com.smartims.entity.User;
import com.smartims.enums.Role;
import com.smartims.exception.AuthException;
import com.smartims.repository.UserRepository;
import com.smartims.security.JwtService;
import com.smartims.service.AuditLogService;
import com.smartims.service.EmailService;
import com.smartims.service.PendingRegisterStore;
import com.smartims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PendingRegisterStore pendingRegisterStore;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    @Value("${app.frontend.login-url:http://localhost:5173/login}")
    private String frontendLoginUrl;

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

        auditLogService.logSystem(
                "USER_LOGIN_SUCCESS",
                "User logged in successfully",
                user.getId(),
                "USER"
        );

        return new LoginResponse(token, user.getRole(), user.isMustChangePassword());
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
        pending.setRole(Role.SUPER_ADMIN);

        pendingRegisterStore.save(request.getEmail(), pending);

        auditLogService.logSystem(
                "USER_REGISTRATION_INITIATED",
                "User registration initiated for email " + request.getEmail(),
                null,
                "USER"
        );
    }

    @Override
    public void createUserFromPending(PendingRegisterUser pending) {

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setFullName(pending.getFullName());
        user.setPassword(passwordEncoder.encode(pending.getPassword()));
        user.setRole(Role.SUPER_ADMIN);
        user.setEnabled(true);
        user.setVerified(true);
        user.setLocked(false);

        User savedUser = userRepository.save(user);

        auditLogService.log(
                "USER_CREATED",
                "USER",
                savedUser.getId(),
                "User account created from pending registration"
        );
    }

    @Override
    public void enableUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        user.setVerified(true);

        userRepository.save(user);

        auditLogService.log(
                "USER_ENABLED",
                "USER",
                user.getId(),
                "User account enabled"
        );
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
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        auditLogService.logSystem(
                "PASSWORD_RESET",
                "Password reset completed",
                user.getId(),
                "USER"
        );
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String creatorEmail = auth != null ? auth.getName() : null;
        User creator = creatorEmail != null
                ? userRepository.findByEmail(creatorEmail).orElse(null)
                : null;

        Role targetRole = request.getRole();
        if (targetRole == null) {
          targetRole = Role.USER;
        }

        String targetCompany = request.getCompany();
        if (creator != null && creator.getRole() != Role.SUPER_ADMIN) {
            targetCompany = creator.getCompany();
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(targetRole)
                .company(targetCompany)
                .enabled(true)
                .locked(false)
                .mustChangePassword(true)
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.log(
                "USER_CREATED_BY_ADMIN",
                "USER",
                savedUser.getId(),
                "User created by admin with role " + savedUser.getRole()
        );

        // Email credentials + login link to the newly created user
        try {
            emailService.sendNewUserCredentialsEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    request.getPassword(),
                    frontendLoginUrl
            );
        } catch (Exception ex) {
            // Do not fail user creation if email fails; just audit-log it.
            auditLogService.logSystem(
                    "NEW_USER_CREDENTIALS_EMAIL_FAILED",
                    "Failed to send credentials email to " + savedUser.getEmail(),
                    savedUser.getId(),
                    "EMAIL"
            );
        }

        return mapToResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;

        if (email == null) {
            return userRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return userRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        String company = currentUser.getCompany();

        if (company == null || company.isBlank()) {
            return userRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return userRepository.findByCompany(company)
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

        if (request.getEmail() != null) {
            String nextEmail = request.getEmail().trim();
            if (!nextEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(nextEmail)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(nextEmail);
            }
        }

        if (request.getRole() != null)
            user.setRole(request.getRole());

        if (request.getCompany() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth != null ? auth.getName() : null;
            User currentUser = email != null
                    ? userRepository.findByEmail(email).orElse(null)
                    : null;

            if (currentUser != null && currentUser.getRole() == Role.SUPER_ADMIN) {
                user.setCompany(request.getCompany());
            }
        }

        if (request.getEnabled() != null)
            user.setEnabled(request.getEnabled());

        if (request.getLocked() != null)
            user.setLocked(request.getLocked());

        User updatedUser = userRepository.save(user);

        auditLogService.log(
                "USER_UPDATED",
                "USER",
                updatedUser.getId(),
                "User details updated"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        auditLogService.log(
                "USER_DELETED",
                "USER",
                id,
                "User account deleted: " + user.getEmail()
        );
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
    public LoginResponse changePassword(ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        auditLogService.log(
                "CHANGE_PASSWORD",
                "USER",
                user.getId(),
                "User changed password"
        );

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getRole(), user.isMustChangePassword());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .company(user.getCompany())
                .build();
    }
}
