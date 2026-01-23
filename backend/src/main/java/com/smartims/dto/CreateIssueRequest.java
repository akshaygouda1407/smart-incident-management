package com.smartims.dto;

import com.smartims.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateIssueRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Severity severity;
}
