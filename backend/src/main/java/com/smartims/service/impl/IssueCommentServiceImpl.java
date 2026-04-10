package com.smartims.service.impl;

import com.smartims.dto.IssueCommentResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.IssueComment;
import com.smartims.entity.User;
import com.smartims.enums.Role;
import com.smartims.repository.IssueCommentRepository;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.security.IssueAccessGuard;
import com.smartims.service.AuditLogService;
import com.smartims.service.IssueCommentService;
import com.smartims.service.NotificationInboxService;
import com.smartims.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;
    private final IssueAccessGuard issueAccessGuard;

    @Override
    public IssueCommentResponse addComment(Long issueId, String commentText) {

        String email = AuthUtil.getLoggedInUser();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        Issue issue = issueAccessGuard.requireIssueAccess(issueId);

        IssueComment comment = new IssueComment();
        comment.setComment(commentText);
        comment.setUser(user);
        comment.setIssue(issue);
        comment.setCommentedBy(user);

        IssueComment savedComment = issueCommentRepository.save(comment);

        notificationInboxService.notifyForIssueEvent(
                "ISSUE_COMMENT_ADDED",
                "New comment added to issue: " + issue.getTitle(),
                issue
        );

        auditLogService.log(
                "ISSUE_COMMENT_ADDED",
                "ISSUE",
                issue.getId(),
                "Comment added by " + user.getFullName()
        );

        return map(savedComment);
    }

    @Override
    public List<IssueCommentResponse> getComments(Long issueId) {
        String email = AuthUtil.getLoggedInUser();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        issueAccessGuard.requireIssueAccess(issueId);

        return issueCommentRepository.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .filter(comment -> canViewComment(currentUser, comment))
                .map(this::map)
                .toList();
    }

    private boolean canViewComment(User currentUser, IssueComment comment) {
        if (currentUser == null) return false;
        if (currentUser.getRole() != Role.USER) {
            return true;
        }
        if (comment != null && comment.getCommentedBy() != null
                && comment.getCommentedBy().getId() != null
                && comment.getCommentedBy().getId().equals(currentUser.getId())) {
            return true;
        }
        Role authorRole = comment != null && comment.getCommentedBy() != null
                ? comment.getCommentedBy().getRole()
                : null;
        return authorRole != Role.ENGINEER && authorRole != Role.MANAGER;
    }

    private IssueCommentResponse map(IssueComment c) {
        return IssueCommentResponse.builder()
                .id(c.getId())
                .comment(c.getComment())
                .commentedBy(c.getCommentedBy().getFullName())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
