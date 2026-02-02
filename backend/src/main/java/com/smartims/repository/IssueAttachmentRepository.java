package com.smartims.repository;

import com.smartims.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueAttachmentRepository
        extends JpaRepository<IssueAttachment, Long> {

    List<IssueAttachment> findByIssueId(Long issueId);
}
