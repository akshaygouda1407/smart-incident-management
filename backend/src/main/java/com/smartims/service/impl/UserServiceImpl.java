package com.smartims.service.impl;

import com.smartims.dto.RegisterRequest;
import com.smartims.dto.RegisterResponse;
import com.smartims.entity.User;
import com.smartims.repository.UserRepository;
import com.smartims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.smartims.dto.LoginRequest;
import com.smartims.dto.LoginResponse;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        return new RegisterResponse("User registered successfully");
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        boolean passwordMatch = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatch) {
            throw new RuntimeException("Invalid email or password");
        }

        return new LoginResponse(
                "Login successful",
                user.getRole().name()
        );
    }
}
