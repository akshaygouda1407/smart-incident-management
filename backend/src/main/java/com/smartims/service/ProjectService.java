package com.smartims.service;

import com.smartims.dto.CreateProjectRequest;
import com.smartims.dto.ProjectResponse;
import com.smartims.dto.UpdateProjectRequest;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    List<ProjectResponse> getProjectsForCurrentUser();

    List<ProjectResponse> getAllProjects();

    ProjectResponse getProjectById(Long id);

    ProjectResponse updateProject(Long id, UpdateProjectRequest request);

    void deleteProject(Long id);
}
