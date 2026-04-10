package com.smartims.service.impl;

import com.smartims.entity.Project;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.enums.Role;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.ProjectRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public long getEngineerWorkload(Long engineerId) {

        User currentUser = getCurrentUser();

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        if (currentUser.getRole() == Role.ENGINEER
                && !currentUser.getId().equals(engineer.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.MANAGER
                && currentUser.getRole() != Role.ENGINEER) {
            throw new UnauthorizedException("Access denied");
        }

        String company = requireCompany(currentUser);
        if (engineer.getCompany() == null || !company.equals(engineer.getCompany())) {
            throw new UnauthorizedException("Access denied: Engineer belongs to a different company");
        }

        return issueRepository.countByAssignedEngineerAndStatusInAndProject_Company(
                engineer,
                List.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS),
                company
        );
    }

    @Override
    public long getManagerWorkload(Long managerId) {

        User currentUser = getCurrentUser();

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (currentUser.getRole() == Role.MANAGER
                && !currentUser.getId().equals(manager.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER) {
            throw new UnauthorizedException("Access denied");
        }

        String company = requireCompany(currentUser);
        if (manager.getCompany() == null || !company.equals(manager.getCompany())) {
            throw new UnauthorizedException("Access denied: Manager belongs to a different company");
        }

        List<Project> projects = projectRepository.findByManager(manager);

        if (projects.isEmpty()) {
            return 0;
        }

        return issueRepository.countByProjectInAndStatusIn(
                projects,
                List.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS)
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Authenticated user not found");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private String requireCompany(User user) {
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }
}
