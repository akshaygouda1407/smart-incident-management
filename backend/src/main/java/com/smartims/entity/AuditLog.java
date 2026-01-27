package com.smartims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHO
    @Column(nullable = false)
    private String actorEmail;

    // ROLE (ADMIN / MANAGER / ENGINEER / USER)
    @Column(nullable = false)
    private String actorRole;

    // WHAT (CREATE_ISSUE, UPDATE_STATUS, SLA_BREACHED, etc.)
    @Column(nullable = false)
    private String action;

    // ENTITY TYPE (ISSUE / PROJECT / SLA)
    @Column(nullable = false)
    private String entityType;

    // ENTITY ID
    private Long entityId;

    // OPTIONAL DESCRIPTION
    @Column(length = 1000)
    private String description;

    // WHEN
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
