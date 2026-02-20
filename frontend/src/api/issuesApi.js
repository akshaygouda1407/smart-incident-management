import api from "./axios";

// Backend controller: /api/issues
export const getAllIssues = () => api.get("/issues");

export const getSlaComplianceSummary = () => api.get("/issues/sla/compliance");

export const getIssueSlaStatus = (id) => api.get(`/issues/${id}/sla-status`);

