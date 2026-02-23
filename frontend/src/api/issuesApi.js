import api from "./axios";

// Backend controller: /api/issues
export const getAllIssues = () => api.get("/issues");

export const createIssue = (payload) => api.post("/issues", payload);
export const updateIssueDetails = (id, payload) => api.put(`/issues/${id}`, payload);

export const getIssuesByProject = (projectId) => api.get(`/issues/project/${projectId}`);
export const getIssueById = (id) => api.get(`/issues/${id}`);
export const getIssueTimeline = (issueId) => api.get(`/issues/${issueId}/timeline`);
export const getIssueAttachments = (issueId) => api.get(`/issues/${issueId}/attachments`);

export const uploadIssueAttachment = (issueId, file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post(`/issues/${issueId}/attachments`, formData);
};

export const getIssueAttachmentDownloadUrl = (downloadUrl) => {
  const base = String(api.defaults.baseURL || "http://localhost:8080/api");
  const origin = base.replace(/\/api\/?$/, "/");
  return new URL(downloadUrl, origin).toString();
};

export const getSlaComplianceSummary = () => api.get("/issues/sla/compliance");

export const getIssueSlaStatus = (id) => api.get(`/issues/${id}/sla-status`);
export const updateIssueSeverity = (id, severity) => api.put(`/issues/${id}/severity`, { severity });
export const assignIssueToEngineer = (id, engineerId) => api.post(`/issues/${id}/assign`, { engineerId });
export const getManagerAssignmentBoard = () => api.get("/issues/manager/assignment-board");
export const autoAssignManagerUnassigned = () => api.post("/issues/manager/auto-assign-unassigned");

export const updateIssueStatus = (id, status) => api.put(`/issues/${id}/status`, { status });
export const addIssueComment = (id, comment) => api.post(`/issues/${id}/comments`, { comment });
export const getIssueComments = (id) => api.get(`/issues/${id}/comments`);
