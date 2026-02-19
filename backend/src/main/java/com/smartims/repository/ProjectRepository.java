package com.smartims.repository;

import com.smartims.entity.Project;
import com.smartims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByManager(User manager);

    List<Project> findByMembersContaining(User user);

    @Query("SELECT DISTINCT p FROM Project p WHERE p.manager.company = :company OR p.id IN (SELECT m.id FROM Project p JOIN p.members m WHERE m.company = :company)")
    List<Project> findByCompany(@Param("company") String company);
}
