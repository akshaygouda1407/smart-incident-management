package com.smartims.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EngineerWorkloadResponse {

    private Long engineerId;
    private String engineerName;
    private long openIssues;
    private long inProgressIssues;

    public EngineerWorkloadResponse() {
    }

    public EngineerWorkloadResponse(Long engineerId, String engineerName,
                                    long openIssues, long inProgressIssues) {
        this.engineerId = engineerId;
        this.engineerName = engineerName;
        this.openIssues = openIssues;
        this.inProgressIssues = inProgressIssues;
    }

}