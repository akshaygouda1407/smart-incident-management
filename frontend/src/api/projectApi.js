import api from "./axios";

// Backend controller: /api/projects
export const getAllProjects = () => api.get("/projects");

export const getProjectById = (id) => api.get(`/projects/${id}`);

export const createProject = (payload) => api.post("/projects", payload);

export const updateProject = (id, payload) => api.put(`/projects/${id}`, payload);

export const deleteProject = (id) => api.delete(`/projects/${id}`);

