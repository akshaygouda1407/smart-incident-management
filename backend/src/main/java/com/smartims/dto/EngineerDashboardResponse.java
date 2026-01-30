package com.smartims.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EngineerDashboardResponse {

    private long totalAssigned;
    private long open;
    private long inProgress;
    private long closed;

    public EngineerDashboardResponse() {}

}
