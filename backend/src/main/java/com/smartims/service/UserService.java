package com.smartims.service;

import com.smartims.dto.RegisterRequest;
import com.smartims.dto.RegisterResponse;

public interface UserService {

    RegisterResponse registerUser(RegisterRequest request);
}
