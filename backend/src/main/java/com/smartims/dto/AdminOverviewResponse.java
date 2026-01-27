package com.smartims.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminOverviewResponse {

    private long totalIssues;
    private long openIssues;
    private long inProgressIssues;
    private long closedIssues;
    private long slaBreached;

    public AdminOverviewResponse() {
    }

}
