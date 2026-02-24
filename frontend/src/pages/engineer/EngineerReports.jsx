import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCheck,
  CheckCircle2,
  CircleDot,
  ClipboardList,
  PlayCircle,
  RefreshCcw,
  ShieldAlert,
  ShieldCheck,
  TimerReset
} from "lucide-react";
import { getAllIssues, getIssueSlaStatus } from "../../api/issuesApi";
import { getAllProjects } from "../../api/projectApi";
import { showError } from "../../utils/toast";

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

function formatStatus(value) {
  return String(value || "-")
    .toLowerCase()
    .split("_")
    .map((part) => (part ? part[0].toUpperCase() + part.slice(1) : part))
    .join(" ");
}

function issueCode(id) {
  const num = Number(id);
  if (Number.isNaN(num)) return "-";
  return `ISS-${String(num).padStart(3, "0")}`;
}

function parseDateMs(value) {
  if (!value) return null;
  const normalized = typeof value === "string" ? value.replace(" ", "T") : value;
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date.getTime();
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

function priorityPillClasses(priority) {
  const normalized = String(priority || "").toUpperCase();
  if (normalized === "CRITICAL") return "bg-red-500 text-white";
  if (normalized === "HIGH") return "bg-amber-500 text-white";
  if (normalized === "MEDIUM") return "bg-blue-500 text-white";
  if (normalized === "LOW") return "bg-gray-200 text-gray-700";
  return "bg-gray-100 text-gray-700";
}

function statusBadgeClass(status) {
  const key = String(status || "").toUpperCase();
  if (key === "OPEN") return "border-blue-300 bg-blue-100 text-blue-700";
  if (key === "IN_PROGRESS") return "border-amber-300 bg-amber-100 text-amber-700";
  if (key === "RESOLVED") return "border-emerald-300 bg-emerald-100 text-emerald-700";
  if (key === "CLOSED") return "border-slate-300 bg-slate-100 text-slate-700";
  if (key === "CREATED") return "border-indigo-300 bg-indigo-100 text-indigo-700";
  return "border-gray-300 bg-gray-100 text-gray-700";
}

function slaBadgeClass(status) {
  const key = String(status || "").toUpperCase();
  if (key === "ON_TRACK") return "bg-emerald-100 text-emerald-700";
  if (key === "AT_RISK") return "bg-amber-100 text-amber-700";
  if (key === "BREACHED") return "bg-red-100 text-red-700";
  if (key === "NOT_STARTED") return "bg-indigo-100 text-indigo-700";
  return "bg-gray-100 text-gray-700";
}

function FilterCard({ icon: Icon, label, value, hint, active, onClick, tone }) {
  const tones = {
    indigo: {
      card: "border-indigo-200 bg-indigo-50 dark:border-indigo-500/30 dark:bg-indigo-500/10",
      label: "text-indigo-700 dark:text-indigo-300",
      value: "text-indigo-900 dark:text-indigo-100",
      hint: "text-indigo-700/80 dark:text-indigo-300/80",
      icon: "bg-indigo-100 text-indigo-700 dark:bg-indigo-500/20 dark:text-indigo-300"
    },
    blue: {
      card: "border-blue-200 bg-blue-50 dark:border-blue-500/30 dark:bg-blue-500/10",
      label: "text-blue-700 dark:text-blue-300",
      value: "text-blue-900 dark:text-blue-100",
      hint: "text-blue-700/80 dark:text-blue-300/80",
      icon: "bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-300"
    },
    amber: {
      card: "border-amber-200 bg-amber-50 dark:border-amber-500/30 dark:bg-amber-500/10",
      label: "text-amber-700 dark:text-amber-300",
      value: "text-amber-900 dark:text-amber-100",
      hint: "text-amber-700/80 dark:text-amber-300/80",
      icon: "bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300"
    },
    emerald: {
      card: "border-emerald-200 bg-emerald-50 dark:border-emerald-500/30 dark:bg-emerald-500/10",
      label: "text-emerald-700 dark:text-emerald-300",
      value: "text-emerald-900 dark:text-emerald-100",
      hint: "text-emerald-700/80 dark:text-emerald-300/80",
      icon: "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-300"
    },
    slate: {
      card: "border-slate-200 bg-slate-50 dark:border-slate-500/30 dark:bg-slate-500/10",
      label: "text-slate-700 dark:text-slate-300",
      value: "text-slate-900 dark:text-slate-100",
      hint: "text-slate-700/80 dark:text-slate-300/80",
      icon: "bg-slate-200 text-slate-700 dark:bg-slate-500/20 dark:text-slate-300"
    },
    red: {
      card: "border-red-200 bg-red-50 dark:border-red-500/30 dark:bg-red-500/10",
      label: "text-red-700 dark:text-red-300",
      value: "text-red-900 dark:text-red-100",
      hint: "text-red-700/80 dark:text-red-300/80",
      icon: "bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300"
    }
  };
  const palette = tones[tone] || tones.indigo;
  return (
    <button
      type="button"
      onClick={onClick}
      className={`w-full rounded-2xl border p-4 text-left transition hover:shadow-sm ${palette.card} ${
        active ? "ring-2 ring-indigo-300 dark:ring-indigo-400" : ""
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <p className={`text-xs font-semibold uppercase tracking-wide ${palette.label}`}>{label}</p>
        {Icon ? (
          <span className={`inline-flex h-8 w-8 items-center justify-center rounded-lg ${palette.icon}`}>
            <Icon className="h-4 w-4" />
          </span>
        ) : null}
      </div>
      <p className={`mt-1 text-3xl font-bold ${palette.value}`}>{value}</p>
      <p className={`mt-1 text-xs ${palette.hint}`}>{hint}</p>
    </button>
  );
}

export default function EngineerReports() {
  const [issues, setIssues] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [projectFilter, setProjectFilter] = useState("ALL");
  const [priorityFilter, setPriorityFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [slaFilter, setSlaFilter] = useState("ALL");

  const fetchData = async ({ silent = false } = {}) => {
    if (!silent) setLoading(true);
    if (silent) setRefreshing(true);
    setError("");
    try {
      const [issuesRes, projectsRes] = await Promise.all([getAllIssues(), getAllProjects()]);
      const baseIssues = unwrapArrayData(issuesRes);
      const projectsData = unwrapArrayData(projectsRes);

      const withSla = await Promise.all(
        baseIssues.map(async (issue) => {
          try {
            const res = await getIssueSlaStatus(issue.id);
            const sla = unwrapData(res) || {};
            return {
              ...issue,
              slaStatus: String(sla?.status || "UNKNOWN").toUpperCase(),
              slaDueTime: sla?.slaDueTime || null
            };
          } catch {
            return {
              ...issue,
              slaStatus: "UNKNOWN",
              slaDueTime: null
            };
          }
        })
      );

      setIssues(withSla);
      setProjects(projectsData);
    } catch (err) {
      const msg = getApiMessage(err);
      setError(msg);
      showError(msg);
      setIssues([]);
      setProjects([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const projectOptions = useMemo(() => {
    const map = new Map();
    projects.forEach((project) => {
      if (project?.id == null) return;
      map.set(String(project.id), project?.name || `Project #${project.id}`);
    });
    issues.forEach((issue) => {
      if (issue?.projectId == null) return;
      if (!map.has(String(issue.projectId))) {
        map.set(String(issue.projectId), issue?.projectName || `Project #${issue.projectId}`);
      }
    });
    return Array.from(map.entries())
      .map(([id, name]) => ({ id, name }))
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [issues, projects]);

  const filteredIssues = useMemo(() => {
    const q = searchTerm.trim().toLowerCase();
    return issues.filter((issue) => {
      const projectMatch =
        projectFilter === "ALL" ? true : String(issue?.projectId || "") === String(projectFilter);
      const priorityMatch =
        priorityFilter === "ALL"
          ? true
          : String(issue?.severity || "").toUpperCase() === String(priorityFilter).toUpperCase();
      const statusMatch =
        statusFilter === "ALL"
          ? true
          : String(issue?.status || "").toUpperCase() === String(statusFilter).toUpperCase();
      const slaMatch =
        slaFilter === "ALL"
          ? true
          : String(issue?.slaStatus || "").toUpperCase() === String(slaFilter).toUpperCase();
      const textMatch =
        q.length === 0
          ? true
          : [
              issueCode(issue?.id),
              issue?.title,
              issue?.description,
              issue?.projectName,
              issue?.severity,
              issue?.status
            ]
              .filter(Boolean)
              .some((value) => String(value).toLowerCase().includes(q));
      return projectMatch && priorityMatch && statusMatch && slaMatch && textMatch;
    });
  }, [issues, projectFilter, priorityFilter, statusFilter, slaFilter, searchTerm]);

  const summary = useMemo(() => {
    const source =
      projectFilter === "ALL"
        ? issues
        : issues.filter((issue) => String(issue?.projectId || "") === String(projectFilter));
    return {
      total: source.length,
      open: source.filter((i) => String(i?.status || "").toUpperCase() === "OPEN").length,
      inProgress: source.filter((i) => String(i?.status || "").toUpperCase() === "IN_PROGRESS").length,
      resolved: source.filter((i) => String(i?.status || "").toUpperCase() === "RESOLVED").length,
      closed: source.filter((i) => String(i?.status || "").toUpperCase() === "CLOSED").length
    };
  }, [issues, projectFilter]);

  const slaSummary = useMemo(() => {
    return filteredIssues.reduce(
      (acc, issue) => {
        const key = String(issue?.slaStatus || "UNKNOWN").toUpperCase();
        if (key === "ON_TRACK") acc.onTrack += 1;
        if (key === "AT_RISK") acc.atRisk += 1;
        if (key === "BREACHED") acc.breached += 1;
        if (key === "NOT_STARTED") acc.notStarted += 1;
        const issueStatus = String(issue?.status || "").toUpperCase();
        if (issueStatus === "RESOLVED" || issueStatus === "CLOSED") {
          const resolvedMs = parseDateMs(issue?.resolvedAt);
          const dueMs = parseDateMs(issue?.slaDueTime);
          if (resolvedMs != null && dueMs != null && resolvedMs <= dueMs) {
            acc.solvedWithinSla += 1;
          }
        }
        return acc;
      },
      { onTrack: 0, atRisk: 0, breached: 0, notStarted: 0, solvedWithinSla: 0 }
    );
  }, [filteredIssues]);

  const statusDistribution = useMemo(() => {
    const keys = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
    const totals = keys.map((key) => ({
      key,
      label: formatStatus(key),
      value: filteredIssues.filter((issue) => String(issue?.status || "").toUpperCase() === key).length
    }));
    const max = Math.max(1, ...totals.map((i) => i.value));
    return { totals, max };
  }, [filteredIssues]);

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Engineer Reports</h1>
          <p className="mt-1 text-sm text-gray-600">Interactive issue and SLA insights for your day-to-day execution.</p>
        </div>
        <button
          type="button"
          onClick={() => fetchData({ silent: true })}
          disabled={refreshing}
          className="inline-flex items-center gap-2 rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-60"
        >
          <RefreshCcw className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
          Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 gap-3 rounded-2xl border border-gray-200 bg-white p-4 lg:grid-cols-4">
        <input
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search by project, priority, description..."
          className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-indigo-500 lg:col-span-2"
        />
        <select
          value={projectFilter}
          onChange={(e) => setProjectFilter(e.target.value)}
          className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="ALL">All projects</option>
          {projectOptions.map((project) => (
            <option key={project.id} value={project.id}>
              {project.name}
            </option>
          ))}
        </select>
        <select
          value={priorityFilter}
          onChange={(e) => setPriorityFilter(e.target.value)}
          className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="ALL">All priorities</option>
          <option value="CRITICAL">Critical</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
      </div>

      <section className="rounded-2xl border border-gray-200 bg-white p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-900">Issue Summary</h2>
          <p className="text-sm text-gray-600">Click a card to filter by status</p>
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-5">
        <FilterCard
          icon={ClipboardList}
          label="Total"
          value={summary.total}
          hint="All tracked issues"
          tone="indigo"
          active={statusFilter === "ALL"}
          onClick={() => setStatusFilter("ALL")}
        />
        <FilterCard
          icon={CircleDot}
          label="Open"
          value={summary.open}
          hint="Waiting to be started"
          tone="blue"
          active={statusFilter === "OPEN"}
          onClick={() => setStatusFilter((prev) => (prev === "OPEN" ? "ALL" : "OPEN"))}
        />
        <FilterCard
          icon={PlayCircle}
          label="In Progress"
          value={summary.inProgress}
          hint="Currently active"
          tone="amber"
          active={statusFilter === "IN_PROGRESS"}
          onClick={() => setStatusFilter((prev) => (prev === "IN_PROGRESS" ? "ALL" : "IN_PROGRESS"))}
        />
        <FilterCard
          icon={CheckCheck}
          label="Resolved"
          value={summary.resolved}
          hint="Marked resolved"
          tone="emerald"
          active={statusFilter === "RESOLVED"}
          onClick={() => setStatusFilter((prev) => (prev === "RESOLVED" ? "ALL" : "RESOLVED"))}
        />
        <FilterCard
          icon={CheckCircle2}
          label="Closed"
          value={summary.closed}
          hint="Final completed"
          tone="slate"
          active={statusFilter === "CLOSED"}
          onClick={() => setStatusFilter((prev) => (prev === "CLOSED" ? "ALL" : "CLOSED"))}
        />
        </div>
      </section>

      <section className="rounded-2xl border border-gray-200 bg-white p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-900">SLA Summary</h2>
          <p className="text-sm text-gray-600">Click a card to filter by SLA status</p>
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-5">
          <FilterCard
            icon={TimerReset}
            label="Not Started SLA"
            value={slaSummary.notStarted}
            hint="Timer not started"
            tone="indigo"
            active={slaFilter === "NOT_STARTED"}
            onClick={() => setSlaFilter((prev) => (prev === "NOT_STARTED" ? "ALL" : "NOT_STARTED"))}
          />
          <FilterCard
            icon={ShieldCheck}
            label="On Track SLA"
            value={slaSummary.onTrack}
            hint="Within due window"
            tone="emerald"
            active={slaFilter === "ON_TRACK"}
            onClick={() => setSlaFilter((prev) => (prev === "ON_TRACK" ? "ALL" : "ON_TRACK"))}
          />
          <FilterCard
            icon={AlertTriangle}
            label="At Risk SLA"
            value={slaSummary.atRisk}
            hint="Near breach"
            tone="amber"
            active={slaFilter === "AT_RISK"}
            onClick={() => setSlaFilter((prev) => (prev === "AT_RISK" ? "ALL" : "AT_RISK"))}
          />
          <FilterCard
            icon={ShieldAlert}
            label="Breached SLA"
            value={slaSummary.breached}
            hint="Due time crossed"
            tone="red"
            active={slaFilter === "BREACHED"}
            onClick={() => setSlaFilter((prev) => (prev === "BREACHED" ? "ALL" : "BREACHED"))}
          />
          <FilterCard
            icon={CheckCircle2}
            label="Solved Within SLA"
            value={slaSummary.solvedWithinSla}
            hint="Resolved/closed before due"
            tone="emerald"
            active={false}
            onClick={() => {}}
          />
        </div>
      </section>

      <section className="rounded-2xl border border-gray-200 bg-white p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-900">Issue Status Counts</h2>
          <p className="text-sm text-gray-600">{filteredIssues.length} issue(s)</p>
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-4">
          {statusDistribution.totals.map((item) => {
            const height = Math.max((item.value / statusDistribution.max) * 100, item.value > 0 ? 8 : 0);
            const active = statusFilter === item.key;
            const statusIcon =
              item.key === "OPEN"
                ? CircleDot
                : item.key === "IN_PROGRESS"
                  ? PlayCircle
                  : item.key === "RESOLVED"
                    ? CheckCheck
                    : CheckCircle2;
            const StatusIcon = statusIcon;
            return (
              <button
                key={item.key}
                type="button"
                onClick={() => setStatusFilter((prev) => (prev === item.key ? "ALL" : item.key))}
                className={`rounded-xl border p-3 text-left transition ${
                  active
                    ? "border-indigo-300 bg-white ring-1 ring-indigo-300 dark:border-indigo-400 dark:bg-slate-900 dark:ring-indigo-400"
                    : "border-gray-200 bg-white hover:bg-gray-50 dark:border-slate-700 dark:bg-slate-900 dark:hover:bg-slate-800"
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-300">
                    {item.label}
                  </p>
                  <span className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-100 text-indigo-700 dark:bg-indigo-500/20 dark:text-indigo-300">
                    <StatusIcon className="h-4 w-4" />
                  </span>
                </div>
                <p className="mt-1 text-2xl font-bold text-gray-900 dark:text-slate-100">{item.value}</p>
                <div className="mt-3 h-2 w-full rounded-full bg-gray-100 dark:bg-slate-700">
                  <div className="h-2 rounded-full bg-indigo-500" style={{ width: `${height}%` }} />
                </div>
              </button>
            );
          })}
        </div>
      </section>

      <section className="overflow-x-auto rounded-2xl border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Issue</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Project</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Priority</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Status</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">SLA</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Created At</th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Resolved At</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {loading ? (
              <tr>
                <td className="px-4 py-8 text-sm text-gray-600" colSpan={7}>
                  Loading reports...
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td className="px-4 py-8 text-sm text-red-700" colSpan={7}>
                  Failed to load reports: {error}
                </td>
              </tr>
            ) : filteredIssues.length === 0 ? (
              <tr>
                <td className="px-4 py-8 text-center text-sm text-gray-600" colSpan={7}>
                  No issues found for selected filters.
                </td>
              </tr>
            ) : (
              filteredIssues.map((issue) => (
                <tr key={issue.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <p className="text-sm font-semibold text-gray-900">{issueCode(issue.id)}</p>
                    <p className="mt-0.5 text-xs text-gray-600">{issue.title || "-"}</p>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700">{issue.projectName || "-"}</td>
                  <td className="px-4 py-3 text-sm">
                    <span className={`inline-flex rounded-md px-2.5 py-1 text-xs font-semibold ${priorityPillClasses(issue.severity)}`}>
                      {formatStatus(issue.severity)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span className={`inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${statusBadgeClass(issue.status)}`}>
                      {formatStatus(issue.status)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span className={`inline-flex rounded-md px-2.5 py-1 text-xs font-semibold ${slaBadgeClass(issue.slaStatus)}`}>
                      {formatStatus(issue.slaStatus)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700">{formatDateTime(issue.createdAt)}</td>
                  <td className="px-4 py-3 text-sm text-gray-700">{formatDateTime(issue.resolvedAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
