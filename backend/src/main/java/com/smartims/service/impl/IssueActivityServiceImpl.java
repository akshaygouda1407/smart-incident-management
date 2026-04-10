package com.smartims.service.impl;

import com.smartims.dto.IssueActivityResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.IssueActivity;
import com.smartims.entity.User;
import com.smartims.repository.IssueActivityRepository;
import com.smartims.repository.UserRepository;
import com.smartims.security.IssueAccessGuard;
import com.smartims.service.AuditLogService;
import com.smartims.service.IssueActivityService;
import com.smartims.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueActivityServiceImpl implements IssueActivityService {

    private final IssueActivityRepository issueActivityRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final IssueAccessGuard issueAccessGuard;

    @Override
    public void logActivity(Issue issue, String action, String description) {

        String email = AuthUtil.getLoggedInUser();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        IssueActivity activity = new IssueActivity();
        activity.setIssue(issue);
        activity.setAction(action);
        activity.setDescription(description);
        activity.setPerformedBy(user);

        issueActivityRepository.save(activity);

        auditLogService.log(
                "ISSUE_ACTIVITY_LOGGED",
                "ISSUE",
                issue.getId(),
                "Activity logged: " + action + " by " + user.getFullName()
        );
    }

    @Override
    public List<IssueActivityResponse> getTimeline(Long issueId) {

        Issue issue = issueAccessGuard.requireIssueAccess(issueId);

        return issueActivityRepository
                .findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .filter(a -> a != null && a.getIssue() != null && a.getIssue().getId() != null
                        && a.getIssue().getId().equals(issue.getId()))
                .map(a -> IssueActivityResponse.builder()
                        .action(a.getAction())
                        .description(a.getDescription())
                        .performedBy(a.getPerformedBy().getFullName())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }
}
