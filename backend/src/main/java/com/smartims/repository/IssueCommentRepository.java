package com.smartims.repository;

import com.smartims.entity.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

    List<IssueComment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
}
