package com.smartims.security;

import com.smartims.entity.Issue;
import com.smartims.entity.User;
import com.smartims.enums.Role;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueAccessGuard {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public Issue requireIssueAccess(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new UnauthorizedException("Issue not found"));

        User currentUser = requireCurrentUser();

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return issue;
        }

        String userCompany = requireCompany(currentUser);
        String issueCompany = issue.getProject() != null ? issue.getProject().getCompany() : null;
        if (issueCompany == null || !userCompany.equals(issueCompany)) {
            throw new UnauthorizedException("Access denied");
        }

        if (currentUser.getRole() == Role.ADMIN) {
            return issue;
        }

        if (currentUser.getRole() == Role.MANAGER) {
            if (issue.getProject() == null
                    || issue.getProject().getManager() == null
                    || issue.getProject().getManager().getId() == null
                    || !issue.getProject().getManager().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Access denied");
            }
            return issue;
        }

        if (currentUser.getRole() == Role.ENGINEER) {
            if (issue.getAssignedEngineer() == null
                    || issue.getAssignedEngineer().getId() == null
                    || !issue.getAssignedEngineer().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Access denied");
            }
            return issue;
        }

        if (currentUser.getRole() == Role.USER) {
            if (issue.getCreatedBy() == null
                    || !issue.getCreatedBy().equalsIgnoreCase(currentUser.getEmail())) {
                throw new UnauthorizedException("Access denied");
            }
            return issue;
        }

        throw new UnauthorizedException("Access denied");
    }

    public User requireCurrentUser() {
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

