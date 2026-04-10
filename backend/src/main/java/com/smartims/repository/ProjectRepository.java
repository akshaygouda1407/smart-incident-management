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

    List<Project> findByManagerAndCompany(User manager, String company);

    List<Project> findByMembersContainingAndCompany(User user, String company);

    boolean existsByNameAndCompany(String name, String company);

    @Query("SELECT p FROM Project p WHERE p.company = :company")
    List<Project> findByCompany(@Param("company") String company);
}
