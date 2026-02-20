import api from "./axios";

// Backend controller: /api/workload
export const getEngineerWorkload = (engineerId) =>
  api.get(`/workload/engineer/${engineerId}`);

export const getManagerWorkload = (managerId) =>
  api.get(`/workload/manager/${managerId}`);

