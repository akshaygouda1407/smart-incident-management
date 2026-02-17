package com.smartims.dto;

import com.smartims.enums.Role;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean locked;
}
