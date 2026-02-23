package com.smartims.dto;

import com.smartims.enums.Severity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateIssueSeverityRequest {

    @NotNull(message = "Severity is required")
    private Severity severity;
}

