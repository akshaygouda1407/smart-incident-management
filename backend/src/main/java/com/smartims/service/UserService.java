package com.smartims.service;

import com.smartims.dto.*;

public interface UserService {

//    RegisterResponse registerUser(RegisterRequest request);
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void resetPassword(String email, String newPassword);

    void enableUser(String email);

    void ensureUserExists(String email);

    void createUserFromPending(PendingRegisterUser pending);


}
