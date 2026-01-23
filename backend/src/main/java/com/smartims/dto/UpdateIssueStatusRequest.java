package com.smartims.dto;

import com.smartims.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateIssueStatusRequest {

    @NotNull
    private IssueStatus status;
}
