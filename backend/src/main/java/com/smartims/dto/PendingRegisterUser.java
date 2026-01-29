package com.smartims.dto;

import com.smartims.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingRegisterUser {
    private String fullName;
    private String email;
    private String password;
    private Role role;
}
