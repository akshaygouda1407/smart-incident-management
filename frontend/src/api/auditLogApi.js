import api from "./axios";

// Super admin: fetch all audit logs
export const getAllAuditLogs = () => api.get("/audit-logs");

