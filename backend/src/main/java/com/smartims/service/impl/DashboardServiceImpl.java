package com.smartims.service.impl;

import com.smartims.dto.DashboardSummaryResponse;
import com.smartims.dto.KeyValueCountResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.Project;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.enums.Role;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.ProjectRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardSummaryResponse getSummary() {

        User currentUser = getCurrentUser();
        List<Issue> issues = getScopedIssues(currentUser);

        long total = issues.size();
        long open = issues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).count();
        long inProgress = issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        long closed = issues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotal(total);
        response.setOpen(open);
        response.setInProgress(inProgress);
        response.setClosed(closed);
        return response;
    }

    @Override
    public List<KeyValueCountResponse> getStatusDistribution() {
        User currentUser = getCurrentUser();
        List<Issue> issues = getScopedIssues(currentUser);

        Map<String, Long> counts = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getStatus() != null ? i.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(e -> new KeyValueCountResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<KeyValueCountResponse> getSeverityDistribution() {
        User currentUser = getCurrentUser();
        List<Issue> issues = getScopedIssues(currentUser);

        Map<String, Long> counts = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getSeverity() != null ? i.getSeverity().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(e -> new KeyValueCountResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<KeyValueCountResponse> getPriorityDistribution() {
        User currentUser = getCurrentUser();
        List<Issue> issues = getScopedIssues(currentUser);

        Map<String, Long> counts = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> (i.getPriorityLevel() == null || i.getPriorityLevel().isBlank())
                                ? "UNSET"
                                : i.getPriorityLevel(),
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(e -> new KeyValueCountResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public DashboardSummaryResponse getProjectDashboard(Long projectId) {

        User currentUser = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!currentUser.getRole().name().equals("ADMIN")
                && !project.getManager().equals(currentUser)
                && !project.getMembers().contains(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        DashboardSummaryResponse response = new DashboardSummaryResponse();

        response.setTotalIssues(issueRepository.countByProject(project));
        response.setOpenIssues(issueRepository.countByProjectAndStatus(project, IssueStatus.OPEN));
        response.setInProgressIssues(issueRepository.countByProjectAndStatus(project, IssueStatus.IN_PROGRESS));
        response.setClosedIssues(issueRepository.countByProjectAndStatus(project, IssueStatus.CLOSED));
        response.setSlaBreached(issueRepository.countByProjectAndSlaBreachedTrue(project));

        return response;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Authenticated user not found");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private List<Issue> getScopedIssues(User currentUser) {
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return issueRepository.findAll();
        }
        if (currentUser.getRole() == Role.ADMIN) {
            String company = requireCompany(currentUser);
            return issueRepository.findByCompany(company);
        }
        if (currentUser.getRole() == Role.MANAGER) {
            List<Project> managerProjects = projectRepository.findByManager(currentUser);
            return managerProjects.stream()
                    .flatMap(p -> issueRepository.findByProject(p).stream())
                    .toList();
        }
        throw new UnauthorizedException("Access denied");
    }

    private String requireCompany(User user) {
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }
}
