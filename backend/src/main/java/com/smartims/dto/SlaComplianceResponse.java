package com.smartims.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlaComplianceResponse {

    private long totalIssues;
    private long slaMet;
    private long slaBreached;
    private double compliancePercentage;
}
