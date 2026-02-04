package com.smartims.service.impl;

import com.smartims.dto.IssueAttachmentResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.IssueAttachment;
import com.smartims.entity.User;
import com.smartims.repository.IssueAttachmentRepository;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.IssueAttachmentService;
import com.smartims.service.NotificationInboxService;
import com.smartims.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueAttachmentServiceImpl implements IssueAttachmentService {

    private final IssueRepository issueRepository;
    private final IssueAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;

    private final Path root = Paths.get("uploads/issues");

    @Override
    public IssueAttachmentResponse upload(Long issueId, MultipartFile file) {

        String email = AuthUtil.getLoggedInUser();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        try {
            Files.createDirectories(root);

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = root.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            IssueAttachment attachment = new IssueAttachment();
            attachment.setIssue(issue);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFilePath(filePath.toString());
            attachment.setUploadedBy(user);

            IssueAttachment savedAttachment = attachmentRepository.save(attachment);

            notificationInboxService.notifyForIssueEvent(
                    "ATTACHMENT_ADDED",
                    "New attachment added to issue: " + issue.getTitle(),
                    issue
            );

            auditLogService.log(
                    "ISSUE_ATTACHMENT_UPLOADED",
                    "ISSUE",
                    issue.getId(),
                    "Attachment '" + savedAttachment.getFileName()
                            + "' uploaded by " + user.getFullName()
            );

            return map(savedAttachment);

        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }

    @Override
    public List<IssueAttachmentResponse> getAttachments(Long issueId) {
        return attachmentRepository.findByIssueId(issueId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public Resource download(Long attachmentId) {

        IssueAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        try {
            Path path = Paths.get(attachment.getFilePath());
            return new UrlResource(path.toUri());
        } catch (Exception e) {
            throw new RuntimeException("File not found");
        }
    }

    private IssueAttachmentResponse map(IssueAttachment a) {
        return IssueAttachmentResponse.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .uploadedBy(a.getUploadedBy().getFullName())
                .uploadedAt(a.getUploadedAt())
                .downloadUrl("/api/issues/attachments/" + a.getId() + "/download")
                .build();
    }
}
