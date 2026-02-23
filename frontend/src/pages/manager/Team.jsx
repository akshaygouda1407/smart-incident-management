import { useEffect, useMemo, useState } from "react";
import { Eye, RefreshCcw, ShieldCheck, UserRound, UsersRound, Wrench } from "lucide-react";
import { getAllProjects } from "../../api/projectApi";
import { getAllUsers } from "../../api/userApi";
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

function splitMembersByRole(memberDetails, memberNames, userLookup) {
  const engineers = [];
  const users = [];
  const details = Array.isArray(memberDetails) ? memberDetails : [];

  details.forEach((member) => {
    const raw = String(member?.fullName || "").trim();
    const resolved =
      userLookup.byEmail.get(raw.toLowerCase()) ||
      userLookup.byName.get(raw.toLowerCase()) ||
      null;
    const role = String(member?.role || resolved?.role || "").toUpperCase();
    const displayName = resolved?.fullName || raw || "-";
    if (role === "ENGINEER") engineers.push(displayName);
    if (role === "USER") users.push(displayName);
  });

  if (!details.length && Array.isArray(memberNames)) {
    memberNames.forEach((value) => {
      const raw = String(value || "").trim();
      if (!raw) return;
      const resolved =
        userLookup.byEmail.get(raw.toLowerCase()) ||
        userLookup.byName.get(raw.toLowerCase()) ||
        null;
      const role = String(resolved?.role || "").toUpperCase();
      const displayName = resolved?.fullName || raw;
      if (role === "ENGINEER") engineers.push(displayName);
      if (role === "USER") users.push(displayName);
    });
  }

  return {
    engineers: Array.from(new Set(engineers)),
    users: Array.from(new Set(users))
  };
}

function resolveDisplayName(value, lookup) {
  const raw = String(value || "").trim();
  if (!raw) return "-";
  const resolved = lookup.byEmail.get(raw.toLowerCase()) || lookup.byName.get(raw.toLowerCase()) || null;
  return resolved?.fullName || raw;
}

function MemberRows({ items, emptyText }) {
  if (!items.length) {
    return <p className="text-sm text-gray-500 dark:text-slate-400">{emptyText}</p>;
  }
  return (
    <div className="space-y-2">
      {items.map((name) => (
        <div
          key={name}
          className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm font-medium text-gray-700 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200"
        >
          {name}
        </div>
      ))}
    </div>
  );
}

export default function Team() {
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [selectedProjectId, setSelectedProjectId] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError("");
    try {
      const [projectRes, userRes] = await Promise.all([getAllProjects(), getAllUsers()]);
      const projectList = unwrapArrayData(projectRes);
      setProjects(projectList);
      setUsers(unwrapArrayData(userRes));
    } catch (err) {
      const msg = getApiMessage(err);
      setError(msg);
      showError(msg);
      setProjects([]);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const userLookup = useMemo(() => {
    const byEmail = new Map();
    const byName = new Map();
    users.forEach((u) => {
      if (u?.email) byEmail.set(String(u.email).toLowerCase(), u);
      if (u?.fullName) byName.set(String(u.fullName).toLowerCase(), u);
    });
    return { byEmail, byName };
  }, [users]);

  const normalizedProjects = useMemo(
    () =>
      projects.map((project) => {
        const split = splitMembersByRole(project?.memberDetails, project?.memberNames, userLookup);
        return {
          ...project,
          managerDisplayName: resolveDisplayName(project?.managerName, userLookup),
          engineers: split.engineers,
          projectUsers: split.users
        };
      }),
    [projects, userLookup]
  );

  const filteredProjects = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return normalizedProjects;
    return normalizedProjects.filter((project) => {
      const haystack = [
        project?.name,
        project?.description,
        project?.managerDisplayName,
        project?.engineers?.join(" "),
        project?.projectUsers?.join(" ")
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return haystack.includes(q);
    });
  }, [normalizedProjects, search]);

  const selectedProject = useMemo(
    () => filteredProjects.find((p) => String(p.id) === String(selectedProjectId)) || null,
    [filteredProjects, selectedProjectId]
  );

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between dark:border-slate-700 dark:bg-slate-900">
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-slate-100">Team Users</h1>
          <p className="mt-1 text-sm text-gray-600 dark:text-slate-300">
            View engineers and users associated with each assigned project.
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

      <div className="rounded-2xl border border-gray-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by project, manager, engineer, user..."
          className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
        />
      </div>

      <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-white dark:border-slate-700 dark:bg-slate-900">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-slate-700">
          <thead className="bg-gray-50 dark:bg-slate-800">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Project
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Manager
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Engineers
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Users
              </th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-slate-200">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white dark:divide-slate-700 dark:bg-slate-900">
            {loading ? (
              <tr>
                <td className="px-4 py-8 text-sm text-gray-600 dark:text-slate-300" colSpan={5}>
                  Loading team users...
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td className="px-4 py-8 text-sm text-red-700 dark:text-red-300" colSpan={5}>
                  Failed to load data: {error}
                </td>
              </tr>
            ) : filteredProjects.length === 0 ? (
              <tr>
                <td className="px-4 py-8 text-center text-sm text-gray-600 dark:text-slate-300" colSpan={5}>
                  No team mapping found for projects.
                </td>
              </tr>
            ) : (
              filteredProjects.map((project) => (
                <tr key={project.id} className="hover:bg-gray-50 dark:hover:bg-slate-800">
                  <td className="px-4 py-3">
                    <p className="text-sm font-semibold text-gray-900 dark:text-slate-100">{project.name || "-"}</p>
                    <p className="mt-1 line-clamp-1 text-xs text-gray-600 dark:text-slate-300">{project.description || "-"}</p>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">{project.managerDisplayName}</td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">{project.engineers.length}</td>
                  <td className="px-4 py-3 text-sm text-gray-700 dark:text-slate-200">{project.projectUsers.length}</td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => setSelectedProjectId(project.id)}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
                    >
                      <Eye className="h-4 w-4" />
                      View Team
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {selectedProject ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900">
          <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
            <section className="xl:col-span-2 rounded-xl border border-gray-200 bg-gray-50 p-5 dark:border-slate-700 dark:bg-slate-800/60">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-slate-100">{selectedProject.name || "-"}</h2>
              <p className="mt-2 text-sm text-gray-600 dark:text-slate-300">{selectedProject.description || "-"}</p>
            </section>

            <section className="space-y-3 rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-slate-700 dark:bg-slate-800/60">
              <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-slate-600 dark:bg-slate-900">
                <p className="inline-flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-slate-100">
                  <ShieldCheck className="h-4 w-4 text-indigo-600 dark:text-indigo-300" />
                  Manager: {selectedProject.managerDisplayName}
                </p>
              </div>
              <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-slate-600 dark:bg-slate-900">
                <p className="inline-flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-slate-100">
                  <Wrench className="h-4 w-4 text-amber-600 dark:text-amber-300" />
                  Engineers: {selectedProject.engineers.length}
                </p>
              </div>
              <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-slate-600 dark:bg-slate-900">
                <p className="inline-flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-slate-100">
                  <UsersRound className="h-4 w-4 text-emerald-600 dark:text-emerald-300" />
                  Users: {selectedProject.projectUsers.length}
                </p>
              </div>
            </section>
          </div>

          <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
            <section className="rounded-xl border border-gray-200 bg-gray-50 p-5 dark:border-slate-700 dark:bg-slate-800/60">
              <h3 className="mb-3 inline-flex items-center gap-2 text-base font-semibold text-gray-900 dark:text-slate-100">
                <Wrench className="h-4 w-4" />
                Engineer List
              </h3>
              <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-slate-600 dark:bg-slate-900">
                <MemberRows items={selectedProject.engineers} emptyText="No engineers mapped." />
              </div>
            </section>

            <section className="rounded-xl border border-gray-200 bg-gray-50 p-5 dark:border-slate-700 dark:bg-slate-800/60">
              <h3 className="mb-3 inline-flex items-center gap-2 text-base font-semibold text-gray-900 dark:text-slate-100">
                <UserRound className="h-4 w-4" />
                User List
              </h3>
              <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-slate-600 dark:bg-slate-900">
                <MemberRows items={selectedProject.projectUsers} emptyText="No users mapped." />
              </div>
            </section>
          </div>
        </div>
      ) : (
        <div className="rounded-2xl border border-dashed border-gray-300 bg-white p-10 text-center dark:border-slate-600 dark:bg-slate-900">
          <p className="text-sm font-medium text-gray-700 dark:text-slate-200">No project selected</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">Click `View Team` to display project team details.</p>
        </div>
      )}
    </div>
  );
}
