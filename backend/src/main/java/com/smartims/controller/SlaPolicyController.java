package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.dto.SlaResponse;
import com.smartims.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaPolicyController {

    private final SlaService slaService;

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','ENGINEER')")
    public ApiResponse<List<SlaResponse>> getPoliciesForCurrentUser() {
        return ApiResponse.success(
                "SLA policies fetched successfully",
                slaService.getPoliciesForCurrentUser()
        );
    }
}
