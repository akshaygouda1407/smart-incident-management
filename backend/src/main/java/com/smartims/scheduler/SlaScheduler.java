package com.smartims.scheduler;

import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.IssueRepository;
import com.smartims.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final IssueRepository issueRepository;
    private final SlaService slaService;

    @Scheduled(fixedRate = 300000) // every 5 minute
    @Transactional
    public void checkSlaBreaches() {

        List<Issue> openIssues =
                issueRepository.findByStatus(IssueStatus.OPEN);

        for (Issue issue : openIssues) {
            slaService.checkAndMarkBreach(issue);
        }
    }

}
