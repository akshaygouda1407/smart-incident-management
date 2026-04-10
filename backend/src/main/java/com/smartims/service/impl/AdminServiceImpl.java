package com.smartims.service.impl;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.EngineerWorkloadResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.enums.Role;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AdminService;
import com.smartims.service.AuditLogService;
import com.smartims.service.IssueActivityService;
import com.smartims.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User currentUser = getCurrentUser();

        List<Issue> scopedIssues = getScopedIssues(currentUser);

        AdminOverviewResponse response = new AdminOverviewResponse();
        response.setTotalIssues(scopedIssues.size());
        response.setOpenIssues(scopedIssues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).count());
        response.setInProgressIssues(scopedIssues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count());
        response.setClosedIssues(scopedIssues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count());
        response.setSlaBreached(0); // SLA will be added later

        return response;
    }

    @Override
    public List<Issue> getAllIssues() {
        User currentUser = getCurrentUser();
        return getScopedIssues(currentUser);
    }

    @Override
    public void updatePriority(Long issueId, String priority) {
        User currentUser = getCurrentUser();
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        assertIssueInScope(currentUser, issue);

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
        User currentUser = getCurrentUser();
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        assertIssueInScope(currentUser, issue);

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        if (currentUser.getRole() == Role.ADMIN) {
            String company = requireCompany(currentUser);
            if (engineer.getCompany() == null || !company.equals(engineer.getCompany())) {
                throw new UnauthorizedException("Access denied: Engineer belongs to a different company");
            }
        }

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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Authenticated user not found");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private List<Issue> getScopedIssues(User currentUser) {
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return issueRepository.findAll();
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return issueRepository.findByCompany(requireCompany(currentUser));
        }
        throw new UnauthorizedException("Access denied");
    }

    private void assertIssueInScope(User currentUser, Issue issue) {
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Access denied");
        }
        String company = requireCompany(currentUser);
        String issueCompany = issue.getProject() != null ? issue.getProject().getCompany() : null;
        if (issueCompany == null || !company.equals(issueCompany)) {
            throw new UnauthorizedException("Access denied: Issue belongs to a different company");
        }
    }

    private String requireCompany(User user) {
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }
}
