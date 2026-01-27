package com.smartims.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdatePriorityRequest {

    private String priority; // P1, P2, P3, P4

    public UpdatePriorityRequest() {
    }

}
