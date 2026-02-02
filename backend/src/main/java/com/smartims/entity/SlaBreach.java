package com.smartims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_breaches")
@Getter
@Setter
public class SlaBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    private LocalDateTime breachedAt;
    private LocalDateTime slaDueTime;
    private Long delayMinutes;
}
