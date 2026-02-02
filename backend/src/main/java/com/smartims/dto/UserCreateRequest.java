package com.smartims.dto;

import com.smartims.enums.Role;
import lombok.Data;

@Data
public class UserCreateRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role;
}
