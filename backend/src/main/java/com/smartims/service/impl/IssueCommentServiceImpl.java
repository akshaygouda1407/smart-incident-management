package com.smartims.service.impl;

import com.smartims.dto.IssueCommentResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.IssueComment;
import com.smartims.entity.User;
import com.smartims.repository.IssueCommentRepository;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
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

    @Override
    public IssueCommentResponse addComment(Long issueId, String commentText) {

        String email = AuthUtil.getLoggedInUser();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

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
        return issueCommentRepository.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(this::map)
                .toList();
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
