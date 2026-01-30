package com.smartims.service.impl;

import com.smartims.dto.*;
import com.smartims.entity.User;
import com.smartims.enums.OtpPurpose;
import com.smartims.enums.Role;
import com.smartims.exception.AuthException;
import com.smartims.repository.UserRepository;
import com.smartims.service.OtpService;
import com.smartims.service.PendingRegisterStore;
import com.smartims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.smartims.security.JwtService;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PendingRegisterStore pendingRegisterStore;


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

}
