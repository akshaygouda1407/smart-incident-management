import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays, Eye, RefreshCcw, SquarePen, X } from "lucide-react";
import { getAllIssues, updateIssueDetails, updateIssueSeverity } from "../../api/issuesApi";
import { getAllProjects } from "../../api/projectApi";
import { showError, showSuccess } from "../../utils/toast";

const STATUS_OPTIONS = ["CREATED", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const SEVERITY_OPTIONS = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

function getApiMessage(err) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.statusMessage ||
    err?.message ||
    "Something went wrong"
  );
}

function unwrapArrayData(res) {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (Array.isArray(res?.data)) return res.data;
  if (Array.isArray(res?.data?.data)) return res.data.data;
  return [];
}

function unwrapData(res) {
  if (!res) return null;
  if (res?.data?.data !== undefined) return res.data.data;
  if (res?.data !== undefined) return res.data;
  return res;
}

function filterIssuesByProjects(issueList, projectList) {
  const projectIds = new Set((projectList || []).map((p) => String(p?.id)).filter(Boolean));
  return (issueList || []).filter((issue) => projectIds.has(String(issue?.projectId || "")));
}

function formatStatus(value) {
  return String(value || "-")
    .toLowerCase()
    .split("_")
    .map((part) => (part ? part[0].toUpperCase() + part.slice(1) : part))
    .join(" ");
}

function formatDateTime(value) {
  if (!value) return "-";
  const normalized = typeof value === "string" ? value.replace(" ", "T") : value;
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return "-";
  const datePart = new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(date);
  const timePart = new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true
  }).format(date);
  return `${datePart} ${timePart}`;
}

function issueCode(id) {
  const num = Number(id);
  if (Number.isNaN(num)) return "-";
  return `ISS-${String(num).padStart(3, "0")}`;
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
      <div className="relative w-full max-w-2xl rounded-2xl border border-gray-200 bg-white p-5 shadow-xl dark:border-slate-700 dark:bg-slate-900">
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

export default function ManagerIssues() {
  const [issues, setIssues] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [projectFilter, setProjectFilter] = useState("ALL");
  const [severityFilter, setSeverityFilter] = useState("ALL");
  const [savingIssueId, setSavingIssueId] = useState(null);
  const [editOpen, setEditOpen] = useState(false);
  const [editSaving, setEditSaving] = useState(false);
  const [editingIssueId, setEditingIssueId] = useState(null);
  const [editForm, setEditForm] = useState({
    title: "",
    description: "",
    severity: "MEDIUM"
  });
  const [severityOpen, setSeverityOpen] = useState(false);
  const [severitySaving, setSeveritySaving] = useState(false);
  const [severityIssueId, setSeverityIssueId] = useState(null);
  const [severityValue, setSeverityValue] = useState("MEDIUM");
  const navigate = useNavigate();

  const fetchData = async () => {
    setLoading(true);
    setError("");
    try {
      const [issueRes, projectRes] = await Promise.all([getAllIssues(), getAllProjects()]);
      const projectList = unwrapArrayData(projectRes);
      const issueList = unwrapArrayData(issueRes);
      setProjects(projectList);
      setIssues(filterIssuesByProjects(issueList, projectList));
    } catch (err) {
      const msg = getApiMessage(err);
      setError(msg);
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const projectNameById = useMemo(() => {
    const map = new Map();
    projects.forEach((project) => {
      if (project?.id != null) map.set(String(project.id), project?.name || "");
    });
    return map;
  }, [projects]);

  const filteredIssues = useMemo(() => {
    const q = search.trim().toLowerCase();
    return issues.filter((issue) => {
      const creatorRole = String(issue?.createdByRole || "").toUpperCase();
      const isUserCreated = creatorRole ? creatorRole === "USER" : true;
      if (!isUserCreated) return false;
      const projectId = String(issue?.projectId || "");
      const projectName = issue?.projectName || projectNameById.get(projectId) || "";
      const status = String(issue?.status || "").toUpperCase();
      const severity = String(issue?.severity || "").toUpperCase();
      const matchesProject = projectFilter === "ALL" ? true : projectId === projectFilter;
      const matchesStatus = statusFilter === "ALL" ? true : status === statusFilter;
      const matchesSeverity = severityFilter === "ALL" ? true : severity === severityFilter;
      const haystack = `${issue?.title || ""} ${issue?.description || ""} ${projectName} ${status} ${severity} ${issue?.createdByName || ""}`.toLowerCase();
      const matchesSearch = q ? haystack.includes(q) : true;
      return matchesProject && matchesStatus && matchesSeverity && matchesSearch;
    });
  }, [issues, projectFilter, statusFilter, severityFilter, search, projectNameById]);

  const handleSaveSeverity = async (issue, nextSeverityRaw) => {
    const nextSeverity = String(nextSeverityRaw || "").toUpperCase();
    if (!SEVERITY_OPTIONS.includes(nextSeverity)) {
      showError("Please select a valid severity");
      return;
    }
    try {
      setSavingIssueId(issue.id);
      const res = await updateIssueSeverity(issue.id, nextSeverity);
      const updated = unwrapData(res);
      setIssues((prev) =>
        prev.map((item) =>
          item.id === issue.id
            ? {
                ...item,
                severity: updated?.severity || nextSeverity
              }
            : item
        )
      );
      showSuccess(`Severity updated to ${nextSeverity}`);
    } catch (err) {
      showError(getApiMessage(err));
    } finally {
      setSavingIssueId(null);
    }
  };

  const openSeverityCard = (issue) => {
    setSeverityIssueId(issue.id);
    const current = String(issue?.severity || "").toUpperCase();
    setSeverityValue(SEVERITY_OPTIONS.includes(current) ? current : "MEDIUM");
    setSeverityOpen(true);
  };

  const closeSeverityCard = () => {
    if (severitySaving) return;
    setSeverityOpen(false);
    setSeverityIssueId(null);
  };

  const handleSeverityCardSave = async (e) => {
    e.preventDefault();
    if (!severityIssueId) return;
    const issue = issues.find((i) => i.id === severityIssueId);
    if (!issue) return;
    try {
      setSeveritySaving(true);
      await handleSaveSeverity(issue, severityValue);
      closeSeverityCard();
    } finally {
      setSeveritySaving(false);
    }
  };

  const openEdit = (issue) => {
    if (issue?.assignedEngineerId != null) {
      showError("Assigned issues cannot be edited");
      return;
    }
    setEditingIssueId(issue.id);
    setEditForm({
      title: issue?.title || "",
      description: issue?.description || "",
      severity: SEVERITY_OPTIONS.includes(String(issue?.severity || "").toUpperCase())
        ? String(issue.severity).toUpperCase()
        : "MEDIUM"
    });
    setEditOpen(true);
  };

  const closeEdit = () => {
    if (editSaving) return;
    setEditOpen(false);
    setEditingIssueId(null);
  };

  const handleEditSave = async (e) => {
    e.preventDefault();
    if (!editingIssueId) return;
    const title = String(editForm.title || "").trim();
    const description = String(editForm.description || "").trim();
    const severity = String(editForm.severity || "").toUpperCase();
    if (!title) {
      showError("Title is required");
      return;
    }
    if (!description) {
      showError("Description is required");
      return;
    }
    if (!SEVERITY_OPTIONS.includes(severity)) {
      showError("Please select a valid severity");
      return;
    }
    try {
      setEditSaving(true);
      const res = await updateIssueDetails(editingIssueId, { title, description, severity });
      const updated = unwrapData(res);
      setIssues((prev) =>
        prev.map((item) =>
          item.id === editingIssueId
            ? {
                ...item,
                title: updated?.title ?? title,
                description: updated?.description ?? description,
                severity: updated?.severity ?? severity,
                status: updated?.status ?? item.status
              }
            : item
        )
      );
      showSuccess("Issue updated successfully");
      closeEdit();
    } catch (err) {
      showError(getApiMessage(err));
    } finally {
      setEditSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between dark:border-slate-700 dark:bg-slate-900">
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-slate-100">Issues</h1>
          <p className="mt-1 text-sm text-gray-600 dark:text-slate-300">
            View user-created issues and assign severity.
          </p>
        </div>
        <button
          type="button"
          onClick={fetchData}
          className="inline-flex items-center gap-2 rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
        >
          <RefreshCcw className="h-4 w-4" />
          Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 gap-3 rounded-2xl border border-gray-200 bg-white p-4 lg:grid-cols-4 dark:border-slate-700 dark:bg-slate-900">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search issue title, description, reporter..."
          className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 lg:col-span-2 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
        />
        <select
          value={projectFilter}
          onChange={(e) => setProjectFilter(e.target.value)}
          className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
        >
          <option value="ALL">All projects</option>
          {projects.map((project) => (
            <option key={project.id} value={String(project.id)}>
              {project.name}
            </option>
          ))}
        </select>
        <div className="grid grid-cols-2 gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
          >
            <option value="ALL">All status</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {formatStatus(status)}
              </option>
            ))}
          </select>
          <select
            value={severityFilter}
            onChange={(e) => setSeverityFilter(e.target.value)}
            className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
          >
            <option value="ALL">All severity</option>
            {SEVERITY_OPTIONS.map((severity) => (
              <option key={severity} value={severity}>
                {severity}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-white dark:border-slate-700 dark:bg-slate-900">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-slate-700">
          <thead className="bg-gray-50 dark:bg-slate-800">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Issue
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Reporter
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Project
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Status
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Severity
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Created
              </th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white dark:divide-slate-700 dark:bg-slate-900">
            {loading ? (
              <tr>
                <td className="px-4 py-8 text-sm text-gray-600 dark:text-slate-300" colSpan={7}>
                  Loading issues...
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td className="px-4 py-8 text-sm text-red-700 dark:text-red-300" colSpan={7}>
                  Failed to load issues: {error}
                </td>
              </tr>
            ) : filteredIssues.length === 0 ? (
              <tr>
                <td className="px-4 py-8 text-center text-sm text-gray-600 dark:text-slate-300" colSpan={7}>
                  No issues found.
                </td>
              </tr>
            ) : (
              filteredIssues.map((issue) => (
                <tr key={issue.id} className="hover:bg-gray-50 dark:hover:bg-slate-800">
                  <td className="px-4 py-3">
                    <p className="text-sm font-semibold text-gray-900 dark:text-slate-100">
                      {issueCode(issue.id)} - {issue.title || "-"}
                    </p>
                    <p className="mt-1 line-clamp-2 text-xs text-gray-600 dark:text-slate-300">{issue.description || "-"}</p>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">
                    {issue.createdByName || issue.createdBy || "-"}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">
                    {issue.projectName || projectNameById.get(String(issue.projectId || "")) || "-"}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${statusBadgeClass(issue.status)}`}
                    >
                      {formatStatus(issue.status)}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex rounded-md px-2.5 py-1 text-xs font-semibold ${severityBadgeClass(issue.severity)}`}
                    >
                      {issue.severity || "-"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">
                    <span className="inline-flex items-center gap-1.5">
                      <CalendarDays className="h-4 w-4 text-gray-500 dark:text-slate-400" />
                      {formatDateTime(issue.createdAt)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex items-center gap-2">
                      <button
                        type="button"
                        disabled={savingIssueId === issue.id || String(issue?.severity || "").trim() !== "-"}
                        onClick={() => openSeverityCard(issue)}
                        className="rounded-lg border border-indigo-300 bg-indigo-50 px-2.5 py-1.5 text-xs font-semibold text-indigo-700 hover:bg-indigo-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-200 dark:hover:bg-indigo-900/40"
                      >
                        {savingIssueId === issue.id ? "Saving..." : "Severity"}
                      </button>
                      <button
                        type="button"
                        disabled={issue?.assignedEngineerId != null}
                        onClick={() => openEdit(issue)}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-blue-300 bg-blue-50 px-2.5 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-blue-700 dark:bg-blue-900/30 dark:text-blue-200 dark:hover:bg-blue-900/40"
                        title={issue?.assignedEngineerId != null ? "Assigned issues cannot be edited" : "Edit"}
                      >
                        <SquarePen className="h-3.5 w-3.5" />
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => navigate(`/manager/issues/${issue.id}`)}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-2.5 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
                      >
                        <Eye className="h-3.5 w-3.5" />
                        View
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <Modal open={editOpen} onClose={closeEdit} title="Edit Issue">
        <form onSubmit={handleEditSave} className="space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-600 dark:text-slate-300">Title</label>
            <input
              value={editForm.title}
              onChange={(e) => setEditForm((prev) => ({ ...prev, title: e.target.value }))}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
              placeholder="Issue title"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-600 dark:text-slate-300">Description</label>
            <textarea
              rows={4}
              value={editForm.description}
              onChange={(e) => setEditForm((prev) => ({ ...prev, description: e.target.value }))}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
              placeholder="Issue description"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-600 dark:text-slate-300">Severity</label>
            <select
              value={editForm.severity}
              onChange={(e) => setEditForm((prev) => ({ ...prev, severity: e.target.value }))}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
            >
              {SEVERITY_OPTIONS.map((sev) => (
                <option key={sev} value={sev}>
                  {sev}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-center justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={closeEdit}
              className="rounded-xl border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={editSaving}
              className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
            >
              {editSaving ? "Saving..." : "Save changes"}
            </button>
          </div>
        </form>
      </Modal>

      <Modal open={severityOpen} onClose={closeSeverityCard} title="Set Severity">
        <form onSubmit={handleSeverityCardSave} className="space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-600 dark:text-slate-300">Severity</label>
            <select
              value={severityValue}
              onChange={(e) => setSeverityValue(e.target.value)}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
            >
              {SEVERITY_OPTIONS.map((sev) => (
                <option key={sev} value={sev}>
                  {sev}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-center justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={closeSeverityCard}
              className="rounded-xl border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={severitySaving}
              className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
            >
              {severitySaving ? "Saving..." : "Save severity"}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
