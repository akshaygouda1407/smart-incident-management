package com.smartims.service.impl;

import com.smartims.dto.EngineerDashboardResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.EngineerService;
import com.smartims.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineerServiceImpl implements EngineerService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;

    @Override
    public EngineerDashboardResponse getDashboard(String engineerEmail) {

        User engineer = userRepository.findByEmail(engineerEmail)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        EngineerDashboardResponse response = new EngineerDashboardResponse();

        String company = requireCompany(engineer);
        List<Issue> issues = issueRepository.findByAssignedEngineerAndProject_Company(engineer, company);

        response.setTotalAssigned(issues.size());
        response.setOpen(
                issues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).count()
        );
        response.setInProgress(
                issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count()
        );
        response.setClosed(
                issues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count()
        );

        return response;
    }

    @Override
    public List<Issue> getMyIssues(String engineerEmail) {

        User engineer = userRepository.findByEmail(engineerEmail)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        return issueRepository.findByAssignedEngineerAndProject_Company(engineer, requireCompany(engineer));
    }

    @Override
    public void updateIssueStatus(Long issueId, String engineerEmail) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (issue.getAssignedEngineer() == null ||
                !issue.getAssignedEngineer().getEmail().equals(engineerEmail)) {
            throw new RuntimeException("Not authorized to update this issue");
        }

        IssueStatus oldStatus = issue.getStatus();

        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.IN_PROGRESS);
        } else if (issue.getStatus() == IssueStatus.IN_PROGRESS) {
            issue.setStatus(IssueStatus.CLOSED);
        }

        IssueStatus newStatus = issue.getStatus();

        issueRepository.save(issue);

        notificationInboxService.notifyForIssueEvent(
                "ISSUE_STATUS_UPDATED",
                "Issue '" + issue.getTitle()
                        + "' status changed from "
                        + oldStatus + " to " + newStatus,
                issue
        );

        auditLogService.log(
                "ISSUE_STATUS_UPDATED",
                "ISSUE",
                issue.getId(),
                "Engineer updated issue status from "
                        + oldStatus + " to " + newStatus
        );
    }

    private String requireCompany(User user) {
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }
}
