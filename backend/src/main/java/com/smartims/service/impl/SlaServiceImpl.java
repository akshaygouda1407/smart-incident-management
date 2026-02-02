package com.smartims.service.impl;

import com.smartims.dto.SlaStatusResponse;
import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.SlaPolicyRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.NotificationInboxService;
import com.smartims.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaServiceImpl implements SlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final IssueRepository issueRepository;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;

    @Override
    public void applySla(Issue issue) {
        issue.setSlaBreached(false);
    }

    @Override
    public void checkAndMarkBreach(Issue issue) {

        if (issue == null ||
                issue.getCreatedAt() == null ||
                issue.getPriorityLevel() == null ||
                issue.isSlaBreached() ||
                issue.getStatus() == IssueStatus.CLOSED) {
            return;
        }

        slaPolicyRepository
                .findByProjectIdAndPriorityLevel(
                        issue.getProject().getId(),
                        issue.getPriorityLevel()
                )
                .ifPresent(policy -> {

                    long elapsedMinutes = Duration.between(
                            issue.getCreatedAt(),
                            LocalDateTime.now()
                    ).toMinutes();

                    if (elapsedMinutes > policy.getResolutionTimeMinutes()) {

                        issue.setSlaBreached(true);

                        notificationInboxService.notifyForIssueEvent(
                                "SLA_BREACHED",
                                "SLA breached for issue: " + issue.getTitle(),
                                issue
                        );


                        auditLogService.log(
                                "SLA_BREACHED",
                                "ISSUE",
                                issue.getId(),
                                "SLA breached for priority "
                                        + issue.getPriorityLevel()
                                        + " in project "
                                        + issue.getProject().getName()
                        );
                    }
                });
    }

    @Override
    public SlaStatusResponse getSlaStatus(Long issueId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = issue.getSlaStartTime();
        LocalDateTime due = issue.getSlaDueTime();

        if (start == null || due == null) {
            throw new RuntimeException("SLA not initialized for this issue");
        }

        long totalMinutes = Duration.between(start, due).toMinutes();
        long remainingMinutes = Duration.between(now, due).toMinutes();

        String status;

        if (now.isAfter(due)) {
            status = "BREACHED";
            remainingMinutes = 0;
        } else if (remainingMinutes <= totalMinutes * 0.2) {
            status = "AT_RISK";
        } else {
            status = "ON_TRACK";
        }

        return SlaStatusResponse.builder()
                .slaStartTime(start)
                .slaDueTime(due)
                .remainingMinutes(remainingMinutes)
                .status(status)
                .build();
    }

}
