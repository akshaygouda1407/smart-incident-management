package com.smartims.service.impl;

import com.smartims.dto.CreateProjectRequest;
import com.smartims.dto.ProjectMemberResponse;
import com.smartims.dto.ProjectResponse;
import com.smartims.dto.UpdateProjectRequest;
import com.smartims.entity.Project;
import com.smartims.entity.User;
import com.smartims.enums.Role;
import com.smartims.exception.BadRequestException;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.ProjectRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.NotificationInboxService;
import com.smartims.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private static final DateTimeFormatter CREATED_AT_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationInboxService notificationInboxService;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {

        if (request.getManagerId() == null) {
            throw new IllegalArgumentException("Manager ID must not be null");
        }

        User currentUser = getCurrentUser();

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() ->
                        new RuntimeException("Manager not found with id: " + request.getManagerId())
                );

        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new BadRequestException("Project name is required");
        }

        String projectCompany = manager.getCompany();
        if (projectCompany == null || projectCompany.isBlank()) {
            throw new BadRequestException("Manager company is required");
        }
        projectCompany = projectCompany.trim();

        List<User> members = new ArrayList<>();
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            members = userRepository.findAllById(request.getMemberIds());
        }

        // Company admin can create projects only within their company
        if (currentUser.getRole() == Role.ADMIN) {
            String company = requireCompany(currentUser);
            if (manager.getCompany() == null || !company.equals(manager.getCompany())) {
                throw new UnauthorizedException("Access denied: Manager belongs to a different company");
            }
            boolean hasCrossCompanyMember = members.stream()
                    .anyMatch(u -> u.getCompany() == null || !company.equals(u.getCompany()));
            if (hasCrossCompanyMember) {
                throw new UnauthorizedException("Access denied: One or more members belong to a different company");
            }
        }

        if (projectRepository.existsByNameAndCompany(name, projectCompany)) {
            throw new BadRequestException("Project name already exists in this company");
        }

        Project project = Project.builder()
                .name(name)
                .description(request.getDescription())
                .manager(manager)
                .company(projectCompany)
                .members(members)
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);

        notificationInboxService.notifyForProjectEvent(
                "PROJECT_CREATED",
                "Project created: " + savedProject.getName(),
                savedProject
        );

        auditLogService.log(
                "PROJECT_CREATED",
                "PROJECT",
                savedProject.getId(),
                "Project created with manager " + manager.getEmail()
        );

        return mapToResponse(savedProject);
    }

    @Override
    public List<ProjectResponse> getProjectsForCurrentUser() {

        User currentUser = getCurrentUser();

        List<Project> projects;

        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findByCompany(requireCompany(currentUser));
        } else if (currentUser.getRole() == Role.MANAGER) {
            projects = projectRepository.findByManagerAndCompany(currentUser, requireCompany(currentUser));
        } else {
            projects = projectRepository.findByMembersContainingAndCompany(currentUser, requireCompany(currentUser));
        }

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        User currentUser = getCurrentUser();

        List<Project> projects;

        // SUPER_ADMIN can see all projects
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            projects = projectRepository.findAll();
        }
        // ADMIN can see projects from their company only
        else if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findByCompany(requireCompany(currentUser));
        }
        // MANAGER can see only their projects
        else if (currentUser.getRole() == Role.MANAGER) {
            projects = projectRepository.findByManagerAndCompany(currentUser, requireCompany(currentUser));
        }
        // ENGINEER and USER can see only projects they're members of
        else {
            projects = projectRepository.findByMembersContainingAndCompany(currentUser, requireCompany(currentUser));
        }

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProjectById(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check access permissions
        validateProjectAccess(project);

        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check access permissions
        validateProjectAccess(project);

        User currentUser = getCurrentUser();

        String oldName = project.getName();

        if (request.getName() != null) {
            String nextName = request.getName().trim();
            if (nextName.isBlank()) {
                throw new BadRequestException("Project name is required");
            }
            String company = project.getCompany();
            if (company == null || company.isBlank()) {
                throw new BadRequestException("Project company is required");
            }
            company = company.trim();
            if (!nextName.equalsIgnoreCase(oldName)
                    && projectRepository.existsByNameAndCompany(nextName, company)) {
                throw new BadRequestException("Project name already exists in this company");
            }
            project.setName(nextName);
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            if (currentUser.getRole() == Role.ADMIN) {
                String company = requireCompany(currentUser);
                if (manager.getCompany() == null || !company.equals(manager.getCompany())) {
                    throw new UnauthorizedException("Access denied: Manager belongs to a different company");
                }
            }
            project.setManager(manager);
            project.setCompany(manager.getCompany());
        }

        if (request.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(request.getMemberIds());

            if (members.size() != request.getMemberIds().size()) {
                throw new RuntimeException("One or more members not found");
            }
            if (currentUser.getRole() == Role.ADMIN) {
                String company = requireCompany(currentUser);
                boolean hasCrossCompanyMember = members.stream()
                        .anyMatch(u -> u.getCompany() == null || !company.equals(u.getCompany()));
                if (hasCrossCompanyMember) {
                    throw new UnauthorizedException("Access denied: One or more members belong to a different company");
                }
            }
            project.setMembers(members);
        }

        Project updatedProject = projectRepository.save(project);

        notificationInboxService.notifyForProjectEvent(
                "PROJECT_UPDATED",
                "Project updated: " + updatedProject.getName(),
                updatedProject
        );

        auditLogService.log(
                "PROJECT_UPDATED",
                "PROJECT",
                updatedProject.getId(),
                "Project updated (old name: " + oldName + ", new name: " + updatedProject.getName() + ")"
        );

        return mapToResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check access permissions
        validateProjectAccess(project);

        notificationInboxService.notifyForProjectEvent(
                "PROJECT_DELETED",
                "Project deleted: " + project.getName(),
                project
        );

        projectRepository.delete(project);

        auditLogService.log(
                "PROJECT_DELETED",
                "PROJECT",
                id,
                "Project deleted: " + project.getName()
        );
    }

    private void validateProjectAccess(Project project) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;

        if (email == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // SUPER_ADMIN can access any project
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        // ADMIN can access projects from their company only
        if (currentUser.getRole() == Role.ADMIN) {
            String userCompany = requireCompany(currentUser);
            String projectCompany = project.getCompany();
            if (projectCompany == null || !userCompany.equals(projectCompany)) {
                throw new UnauthorizedException("Access denied: Project belongs to a different company");
            }
            return;
        }

        // MANAGER can access projects they manage and only from their company
        if (currentUser.getRole() == Role.MANAGER) {
            if (!project.getManager().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Access denied: You can only access projects you manage");
            }
            String userCompany = requireCompany(currentUser);
            String projectCompany = project.getCompany();
            if (projectCompany == null || !userCompany.equals(projectCompany)) {
                throw new UnauthorizedException("Access denied: Project belongs to a different company");
            }
            return;
        }

        // ENGINEER and USER can access projects they're members of
        if (!project.getMembers().contains(currentUser)) {
            throw new UnauthorizedException("Access denied: You are not a member of this project");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private String requireCompany(User user) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return null;
        }
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }

    private ProjectResponse mapToResponse(Project project) {
        LocalDateTime createdAt = project.getCreatedAt() != null ? project.getCreatedAt() : LocalDateTime.now();
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .managerName(
                        project.getManager() != null
                                ? project.getManager().getFullName()
                                : null
                )
                .memberNames(
                        project.getMembers()
                                .stream()
                                .map(User::getFullName)
                                .collect(Collectors.toList())
                )
                .memberDetails(
                        project.getMembers()
                                .stream()
                                .map(member -> ProjectMemberResponse.builder()
                                        .id(member.getId())
                                        .fullName(member.getFullName())
                                        .role(member.getRole() != null ? member.getRole().name() : null)
                                        .build())
                                .collect(Collectors.toList())
                )
                .createdAt(createdAt)
                .createdAtDisplay(createdAt.format(CREATED_AT_DISPLAY_FORMAT))
                .debugCreatedAtRaw(String.valueOf(project.getCreatedAt()))
                .debugCreatedAtJavaType(
                        project.getCreatedAt() == null ? "null" : project.getCreatedAt().getClass().getName()
                )
                .build();
    }
}
