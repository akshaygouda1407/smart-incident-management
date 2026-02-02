package com.smartims.service;

import com.smartims.dto.SlaStatusResponse;
import com.smartims.entity.Issue;

public interface SlaService {

    void applySla(Issue issue);

    void checkAndMarkBreach(Issue issue);

    SlaStatusResponse getSlaStatus(Long issueId);
}
