package com.smartims.repository;

import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    long countByStatus(IssueStatus status);

    List<Issue> findByAssignedEngineer(User engineer);

    long countByAssignedEngineer(User engineer);
}
