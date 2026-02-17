package com.smartims.service;

import com.smartims.dto.*;

import java.util.List;

public interface UserService {

//    RegisterResponse registerUser(RegisterRequest request);
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void resetPassword(String email, String newPassword);

    void enableUser(String email);

    void ensureUserExists(String email);

    void createUserFromPending(PendingRegisterUser pending);

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void updateUserStatus(Long userId, boolean enabled);

    void updateUserLockStatus(Long userId, boolean locked);

    LoginResponse changePassword(ChangePasswordRequest request);


}
