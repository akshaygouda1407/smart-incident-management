package com.smartims.repository;

import com.smartims.entity.Issue;
import com.smartims.entity.SlaBreach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaBreachRepository extends JpaRepository<SlaBreach, Long> {

    boolean existsByIssue(Issue issue);

    long deleteByIssue_Project_Company(String company);
}
