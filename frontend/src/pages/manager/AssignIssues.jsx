import { useEffect, useMemo, useState } from "react";
import { RefreshCcw, UserPlus, X } from "lucide-react";
import {
  assignIssueToEngineer,
  autoAssignManagerUnassigned,
  getManagerAssignmentBoard
} from "../../api/issuesApi";
import { getAllProjects } from "../../api/projectApi";
import { showError, showSuccess } from "../../utils/toast";

function getApiMessage(err) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.statusMessage ||
    err?.message ||
    "Something went wrong"
  );
}

function unwrapData(res) {
  if (!res) return null;
  if (res?.data?.data !== undefined) return res.data.data;
  if (res?.data !== undefined) return res.data;
  return res;
}

function unwrapArrayData(res) {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (Array.isArray(res?.data)) return res.data;
  if (Array.isArray(res?.data?.data)) return res.data.data;
  return [];
}

function issueCode(id) {
  const num = Number(id);
  if (Number.isNaN(num)) return "-";
  return `ISS-${String(num).padStart(3, "0")}`;
}

function formatStatus(value) {
  return String(value || "-")
    .toLowerCase()
    .split("_")
    .map((part) => (part ? part[0].toUpperCase() + part.slice(1) : part))
    .join(" ");
}

function statusBadgeClass(status) {
  const key = String(status || "").toUpperCase();
  if (key === "CREATED") return "border-indigo-300 bg-indigo-100 text-indigo-700 dark:border-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-200";
  if (key === "OPEN") return "border-blue-300 bg-blue-100 text-blue-700 dark:border-blue-700 dark:bg-blue-900/30 dark:text-blue-200";
  if (key === "IN_PROGRESS") return "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-700 dark:bg-amber-900/30 dark:text-amber-200";
  if (key === "RESOLVED") return "border-emerald-300 bg-emerald-100 text-emerald-700 dark:border-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-200";
  if (key === "CLOSED") return "border-slate-300 bg-slate-100 text-slate-700 dark:border-slate-600 dark:bg-slate-700/70 dark:text-slate-100";
  return "border-gray-300 bg-gray-100 text-gray-700 dark:border-slate-600 dark:bg-slate-700/70 dark:text-slate-100";
}

function severityBadgeClass(severity) {
  const key = String(severity || "").toUpperCase();
  if (key === "CRITICAL") return "bg-red-500 text-white dark:bg-red-600";
  if (key === "HIGH") return "bg-orange-500 text-white dark:bg-orange-600";
  if (key === "MEDIUM") return "bg-sky-500 text-white dark:bg-sky-600";
  if (key === "LOW") return "bg-gray-200 text-gray-700 dark:bg-slate-600 dark:text-slate-100";
  return "bg-gray-200 text-gray-700 dark:bg-slate-600 dark:text-slate-100";
}

function Modal({ open, title, children, onClose }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative w-full max-w-lg rounded-2xl border border-gray-200 bg-white p-5 shadow-xl dark:border-slate-700 dark:bg-slate-900">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-slate-100">{title}</h3>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1 text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

export default function AssignIssues() {
  const [board, setBoard] = useState({ engineers: [], unassignedIssues: [] });
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [projectFilter, setProjectFilter] = useState("");
  const [assignOpen, setAssignOpen] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [autoAssigning, setAutoAssigning] = useState(false);
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [selectedEngineerId, setSelectedEngineerId] = useState("");

  const fetchBoard = async () => {
    setLoading(true);
    setError("");
    try {
      const [boardRes, projectRes] = await Promise.all([getManagerAssignmentBoard(), getAllProjects()]);
      const data = unwrapData(boardRes) || {};
      setBoard({
        engineers: Array.isArray(data?.engineers) ? data.engineers : [],
        unassignedIssues: Array.isArray(data?.unassignedIssues) ? data.unassignedIssues : []
      });
      setProjects(unwrapArrayData(projectRes));
    } catch (err) {
      const msg = getApiMessage(err);
      setError(msg);
      showError(msg);
      setBoard({ engineers: [], unassignedIssues: [] });
      setProjects([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBoard();
  }, []);

  const engineerOptions = useMemo(
    () => {
      const issueProjectId = selectedIssue?.projectId != null ? String(selectedIssue.projectId) : null;
      const targetProjectId = issueProjectId || projectFilter || null;
      const fromBoard = (board.engineers || [])
        .filter((eng) => {
          if (!targetProjectId) return true;
          const projectIds = Array.isArray(eng.projectIds) ? eng.projectIds.map((id) => String(id)) : [];
          return projectIds.includes(targetProjectId);
        })
        .map((eng) => ({
          id: eng.engineerId,
          name: eng.engineerName || "-",
          email: eng.engineerEmail || "-"
        }));

      const selectedProject =
        (projects || []).find((p) => String(p?.id) === String(targetProjectId || "")) || null;
      const fromProjectMembers = (Array.isArray(selectedProject?.memberDetails) ? selectedProject.memberDetails : [])
        .filter((member) => String(member?.role || "").toUpperCase() === "ENGINEER")
        .filter((member) => member?.id != null)
        .map((member) => ({
          id: member.id,
          name: member.fullName || "-",
          email: "-"
        }));

      const merged = new Map();
      [...fromProjectMembers, ...fromBoard].forEach((eng) => {
        merged.set(String(eng.id), eng);
      });

      return Array.from(merged.values()).map((eng) => ({
        id: eng.id,
        name: eng.name || "-",
        email: eng.email || "-"
      }));
    },
    [board.engineers, selectedIssue?.projectId, projectFilter, projects]
  );

  const projectOptions = useMemo(() => {
    return (projects || []).map((project) => ({
      id: String(project.id),
      name: project?.name || `Project #${project.id}`
    }));
  }, [projects]);

  const selectedProjectEngineerIds = useMemo(() => {
    if (!projectFilter) return new Set();
    const project = (projects || []).find((p) => String(p?.id) === String(projectFilter));
    const details = Array.isArray(project?.memberDetails) ? project.memberDetails : [];
    const engineerIds = details
      .filter((member) => String(member?.role || "").toUpperCase() === "ENGINEER")
      .map((member) => String(member?.id))
      .filter(Boolean);
    return new Set(engineerIds);
  }, [projects, projectFilter]);

  const filteredUnassignedIssues = useMemo(() => {
    const items = board.unassignedIssues || [];
    if (!projectFilter) return [];
    return items.filter((issue) => String(issue?.projectId || "") === projectFilter);
  }, [board.unassignedIssues, projectFilter]);

  const filteredEngineerCards = useMemo(() => {
    if (!projectFilter) return [];
    return (board.engineers || []).map((eng) => {
      const projectIds = Array.isArray(eng.projectIds) ? eng.projectIds.map((id) => String(id)) : [];
      const issues = Array.isArray(eng.assignedIssues) ? eng.assignedIssues : [];
      const filteredIssues = issues.filter((issue) => String(issue?.projectId || "") === projectFilter);
      const inSelectedProjectByProjects = projectIds.includes(projectFilter);
      const inSelectedProjectByMemberMap = selectedProjectEngineerIds.has(String(eng.engineerId));
      const inSelectedProject = inSelectedProjectByProjects || inSelectedProjectByMemberMap;
      return { ...eng, assignedIssues: filteredIssues, inSelectedProject };
    }).filter((eng) => eng.inSelectedProject);
  }, [board.engineers, projectFilter, selectedProjectEngineerIds]);

  const openAssignModal = (issue) => {
    setSelectedIssue(issue);
    setSelectedEngineerId(engineerOptions[0]?.id ? String(engineerOptions[0].id) : "");
    setAssignOpen(true);
  };

  const closeAssignModal = () => {
    if (assigning) return;
    setAssignOpen(false);
    setSelectedIssue(null);
    setSelectedEngineerId("");
  };

  const handleAssign = async (e) => {
    e.preventDefault();
    if (!selectedIssue?.id) return;
    if (!selectedEngineerId) {
      showError("Please select an engineer");
      return;
    }
    try {
      setAssigning(true);
      await assignIssueToEngineer(selectedIssue.id, Number(selectedEngineerId));
      showSuccess("Issue assigned successfully");
      closeAssignModal();
      await fetchBoard();
    } catch (err) {
      showError(getApiMessage(err));
    } finally {
      setAssigning(false);
    }
  };

  const handleAutoAssign = async () => {
    if (!projectFilter) {
      showError("Please select a project");
      return;
    }
    try {
      setAutoAssigning(true);
      const res = await autoAssignManagerUnassigned();
      const data = unwrapData(res) || {};
      setBoard({
        engineers: Array.isArray(data?.engineers) ? data.engineers : [],
        unassignedIssues: Array.isArray(data?.unassignedIssues) ? data.unassignedIssues : []
      });
      showSuccess("Unassigned issues auto-assigned");
    } catch (err) {
      showError(getApiMessage(err));
    } finally {
      setAutoAssigning(false);
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between dark:border-slate-700 dark:bg-slate-900">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-slate-100">Engineer Assignments</h1>
          <p className="mt-1 text-sm text-gray-600 dark:text-slate-300">View assigned issues and assign unassigned issues.</p>
        </div>
        <div className="inline-flex items-center gap-2">
          <select
            value={projectFilter}
            onChange={(e) => setProjectFilter(e.target.value)}
            className="rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
          >
            <option value="">Select Project</option>
            {projectOptions.map((project) => (
              <option key={project.id} value={project.id}>
                {project.name}
              </option>
            ))}
          </select>
          <button
            type="button"
            onClick={fetchBoard}
            className="inline-flex items-center gap-2 rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          >
            <RefreshCcw className="h-4 w-4" />
            Refresh
          </button>
          <button
            type="button"
            onClick={handleAutoAssign}
            disabled={autoAssigning || !projectFilter || filteredUnassignedIssues.length === 0}
            className="inline-flex items-center gap-2 rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-800 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          >
            <RefreshCcw className="h-4 w-4" />
            {autoAssigning ? "Auto-Assigning..." : "Auto-Assign"}
          </button>
        </div>
      </div>

      {!projectFilter ? (
        <div className="rounded-2xl border border-dashed border-gray-300 bg-white p-8 text-center dark:border-slate-600 dark:bg-slate-900">
          <p className="text-sm font-medium text-gray-700 dark:text-slate-200">Select a project to view assignments</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">Engineer cards and unassigned issues will appear below.</p>
        </div>
      ) : null}

      {projectFilter ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {loading ? (
          <div className="rounded-2xl border border-gray-200 bg-white p-5 text-sm text-gray-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
            Loading assignments...
          </div>
        ) : error ? (
          <div className="rounded-2xl border border-red-200 bg-red-50 p-5 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-950/30 dark:text-red-300">
            Failed to load assignments: {error}
          </div>
        ) : filteredEngineerCards.length === 0 ? (
          <div className="rounded-2xl border border-gray-200 bg-white p-5 text-sm text-gray-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
            {projectFilter === "ALL"
              ? "No engineers found in your assigned projects."
              : "No engineers found for selected project."}
          </div>
        ) : (
          filteredEngineerCards.map((eng) => {
            const initial = String(eng.engineerName || "E").charAt(0).toUpperCase();
            const issues = Array.isArray(eng.assignedIssues) ? eng.assignedIssues : [];
            return (
              <section key={eng.engineerId} className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-900">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-500 text-base font-bold text-white">
                    {initial}
                  </div>
                  <div>
                    <p className="text-2xl font-semibold text-gray-900 dark:text-slate-100">{eng.engineerName || "-"}</p>
                    <p className="text-sm text-gray-600 dark:text-slate-300">{eng.engineerEmail || "-"}</p>
                  </div>
                </div>
                <div className="mt-4 border-t border-gray-200 pt-3 dark:border-slate-700">
                  <p className="mb-2 text-sm font-medium text-gray-700 dark:text-slate-200">Assigned Issues:</p>
                  <div className="space-y-2">
                    {issues.length ? (
                      issues.map((issue) => (
                        <div key={issue.id} className="flex items-center justify-between rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 dark:border-slate-700 dark:bg-slate-800">
                          <span className="text-sm font-medium text-indigo-700 dark:text-indigo-300">{issueCode(issue.id)}</span>
                          <span className={`inline-flex rounded-md px-2.5 py-1 text-xs font-semibold ${severityBadgeClass(issue.severity)}`}>
                            {issue.severity || "-"}
                          </span>
                        </div>
                      ))
                    ) : (
                      <p className="text-sm text-gray-500 dark:text-slate-400">No assigned issues.</p>
                    )}
                  </div>
                </div>
              </section>
            );
          })
        )}
        </div>
      ) : null}

      {projectFilter ? (
      <section className="overflow-x-auto rounded-2xl border border-gray-200 bg-white dark:border-slate-700 dark:bg-slate-900">
        <div className="border-b border-gray-200 px-4 py-4 dark:border-slate-700">
          <h2 className="text-2xl font-semibold text-gray-900 dark:text-slate-100">
            Unassigned Issues ({filteredUnassignedIssues.length})
          </h2>
        </div>
        <table className="min-w-full divide-y divide-gray-200 dark:divide-slate-700">
          <thead className="bg-gray-50 dark:bg-slate-800">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">Issue ID</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">Title</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">Status</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">Priority</th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white dark:divide-slate-700 dark:bg-slate-900">
            {loading ? (
              <tr>
                <td className="px-4 py-8 text-sm text-gray-600 dark:text-slate-300" colSpan={5}>Loading unassigned issues...</td>
              </tr>
            ) : filteredUnassignedIssues.length === 0 ? (
              <tr>
                <td className="px-4 py-8 text-center text-sm text-gray-600 dark:text-slate-300" colSpan={5}>
                  No unassigned issues for selected project.
                </td>
              </tr>
            ) : (
              filteredUnassignedIssues.map((issue) => (
                <tr key={issue.id} className="hover:bg-gray-50 dark:hover:bg-slate-800">
                  <td className="px-4 py-3 text-sm font-medium text-indigo-700 dark:text-indigo-300">{issueCode(issue.id)}</td>
                  <td className="px-4 py-3 text-sm text-gray-800 dark:text-slate-100">{issue.title || "-"}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${statusBadgeClass(issue.status)}`}>
                      {formatStatus(issue.status)}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex rounded-md px-2.5 py-1 text-xs font-semibold ${severityBadgeClass(issue.severity)}`}>
                      {issue.severity || "-"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => openAssignModal(issue)}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-semibold text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
                    >
                      <UserPlus className="h-4 w-4" />
                      Assign
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
      ) : null}

      <Modal open={assignOpen} onClose={closeAssignModal} title={`Assign ${issueCode(selectedIssue?.id)}`}>
        <form onSubmit={handleAssign} className="space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-600 dark:text-slate-300">Engineer</label>
            <select
              value={selectedEngineerId}
              onChange={(e) => setSelectedEngineerId(e.target.value)}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
            >
              {engineerOptions.map((eng) => (
                <option key={eng.id} value={String(eng.id)}>
                  {eng.name} ({eng.email})
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-center justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={closeAssignModal}
              className="rounded-xl border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={assigning || engineerOptions.length === 0}
              className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
            >
              {assigning ? "Assigning..." : "Assign Issue"}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
