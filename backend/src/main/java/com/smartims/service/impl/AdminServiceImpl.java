package com.smartims.service.impl;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.EngineerWorkloadResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AdminService;
import com.smartims.service.AuditLogService;
import com.smartims.service.IssueActivityService;
import com.smartims.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueActivityService issueActivityService;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;

    @Override
    public AdminOverviewResponse getOverview() {
        AdminOverviewResponse response = new AdminOverviewResponse();

        response.setTotalIssues(issueRepository.count());
        response.setOpenIssues(issueRepository.countByStatus(IssueStatus.OPEN));
        response.setInProgressIssues(issueRepository.countByStatus(IssueStatus.IN_PROGRESS));
        response.setClosedIssues(issueRepository.countByStatus(IssueStatus.CLOSED));
        response.setSlaBreached(0); // SLA will be added later

        return response;
    }

    @Override
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @Override
    public void updatePriority(Long issueId, String priority) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String oldPriority = issue.getPriorityLevel();

        issue.setPriorityLevel(priority);
        issueRepository.save(issue);

        issueActivityService.logActivity(
                issue,
                "PRIORITY_CHANGED",
                "Priority changed from " + oldPriority + " to " + priority
        );

        notificationInboxService.notifyForIssueEvent(
                "ISSUE_PRIORITY_UPDATED",
                "Priority changed from " + oldPriority + " to " + priority
                        + " for issue: " + issue.getTitle(),
                issue
        );

        auditLogService.log(
                "ISSUE_PRIORITY_UPDATED",
                "ISSUE",
                issue.getId(),
                "Priority changed from " + oldPriority + " to " + priority
        );
    }

    @Override
    public void assignEngineer(Long issueId, Long engineerId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        issue.setAssignedEngineer(engineer);
        issueRepository.save(issue);

        issueActivityService.logActivity(
                issue,
                "ENGINEER_ASSIGNED",
                "Engineer " + engineer.getFullName() + " assigned to issue"
        );

        notificationInboxService.notifyForIssueEvent(
                "ENGINEER_ASSIGNED",
                "You have been assigned to issue: " + issue.getTitle(),
                issue
        );

        auditLogService.log(
                "ENGINEER_ASSIGNED",
                "ISSUE",
                issue.getId(),
                "Engineer " + engineer.getFullName()
                        + " assigned to issue by admin"
        );
    }

    @Override
    public List<EngineerWorkloadResponse> getEngineerWorkload() {
        // Will be implemented after EngineerController
        return new ArrayList<>();
    }
}
