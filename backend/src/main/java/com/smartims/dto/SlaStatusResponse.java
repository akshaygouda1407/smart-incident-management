package com.smartims.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SlaStatusResponse {

    private LocalDateTime slaStartTime;
    private LocalDateTime slaDueTime;

    private long remainingMinutes;
    private String status;
}
