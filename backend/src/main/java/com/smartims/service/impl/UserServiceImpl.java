package com.smartims.service.impl;

import com.smartims.dto.*;
import com.smartims.entity.User;
import com.smartims.enums.OtpPurpose;
import com.smartims.enums.Role;
import com.smartims.exception.AuthException;
import com.smartims.exception.BadRequestException;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.UserRepository;
import com.smartims.service.OtpService;
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

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(token, user.getRole().name());
    }

    @Override
    public void register(RegisterRequest request) {

        // 1️⃣ Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        // 2️⃣ Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);

        // 🔐 IMPORTANT: lock account until OTP verified
        user.setEnabled(false);
        user.setVerified(false);

        userRepository.save(user);
    }

    //    @Override
//    public RegisterResponse registerUser(RegisterRequest request) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new AuthException("Invalid email or password");
//        }
//
//        if (!otpService.isOtpVerified(request.getEmail(), OtpPurpose.REGISTER)) {
//            throw new AuthException("Invalid email or password");
//        }
//
//        User user = User.builder()
//                .fullName(request.getFullName())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
//                .build();
//
//        userRepository.save(user);
//
//        return new RegisterResponse(
//                user.getId(),
//                user.getEmail(),
//                user.getRole().name()
//        );
//    }

    @Override
    public void createUserFromPending(PendingRegisterUser pending) {

        User user = new User();
        user.setFullName(pending.getFullName());
        user.setEmail(pending.getEmail());
        user.setPassword(passwordEncoder.encode(pending.getPassword()));
        user.setRole(Role.ADMIN);

        user.setEnabled(true);
        user.setVerified(true);

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
