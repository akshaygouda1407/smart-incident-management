package com.smartims.entity;

import com.smartims.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean locked = false;//if user is deleted then only it should be true(softDelete)

    @Column(nullable = false)
    private Integer tokenVersion = 0;

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }


    public Boolean getLocked() {
        return locked;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}

