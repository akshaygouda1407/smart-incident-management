import { useEffect, useMemo, useState } from "react";
import { createUser, getAllUsers } from "../../api/userApi";
import { showError, showSuccess } from "../../utils/toast";
import LoadingButton from "../../components/common/LoadingButton";

function getApiMessage(err) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.statusMessage ||
    err?.message ||
    "Something went wrong"
  );
}

export default function SuperAdminAddCompanyAdmin() {
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    company: ""
  });
  const [loading, setLoading] = useState(false);

  const [admins, setAdmins] = useState([]);
  const [adminsLoading, setAdminsLoading] = useState(true);
  const [adminsError, setAdminsError] = useState("");
  const [search, setSearch] = useState("");
  const [companyFilter, setCompanyFilter] = useState("ALL");

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const fetchAdmins = async () => {
    setAdminsLoading(true);
    setAdminsError("");
    try {
      const res = await getAllUsers();
      const data = res?.data ?? res;
      const all = Array.isArray(data) ? data : [];
      setAdmins(all.filter((u) => String(u.role) === "ADMIN"));
    } catch (err) {
      const msg = getApiMessage(err);
      setAdminsError(msg);
      showError(msg);
    } finally {
      setAdminsLoading(false);
    }
  };

  useEffect(() => {
    fetchAdmins();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    if (!form.company.trim()) {
      showError("Company is required");
      return;
    }

    setLoading(true);
    try {
      await createUser({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        role: "ADMIN",
        company: form.company.trim()
      });
      showSuccess("Company admin created successfully");
      setForm({
        fullName: "",
        email: "",
        password: "",
        company: ""
      });
      await fetchAdmins();
    } catch (err) {
      const msg = getApiMessage(err);
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  const companies = useMemo(() => {
    const set = new Set(
      admins
        .map((a) => a.company)
        .filter((c) => c && String(c).trim().length > 0)
    );
    return Array.from(set).sort((a, b) => a.localeCompare(b));
  }, [admins]);

  const displayAdmins = useMemo(() => {
    const q = search.trim().toLowerCase();
    return admins.filter((a) => {
      const matchesCompany =
        companyFilter === "ALL" ? true : a.company === companyFilter;
      const hay = `${a.fullName || ""} ${a.email || ""} ${a.company || ""}`.toLowerCase();
      const matchesSearch = q ? hay.includes(q) : true;
      return matchesCompany && matchesSearch;
    });
  }, [admins, search, companyFilter]);

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-6">
      <h1 className="text-xl font-bold text-gray-900">Super Admin · Company Admins</h1>
      <p className="mt-2 text-sm text-gray-600">
        Create and review company-level admins. Each company admin manages projects and users within their own company.
      </p>

      <div className="mt-5 grid gap-6 lg:grid-cols-2">
        {/* Create form */}
        <div className="rounded-xl border border-gray-200 bg-gray-50/60 p-4">
          <h2 className="text-sm font-semibold text-gray-800">Add company admin</h2>

          <form onSubmit={handleSubmit} className="mt-3 space-y-3">
            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-600">
                Full name
              </label>
              <input
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="e.g. Jane Doe"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-600">
                Email
              </label>
              <input
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="e.g. jane@company.com"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-600">
                Password
              </label>
              <input
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Temporary password to send to admin"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-600">
                Company
              </label>
              <input
                name="company"
                value={form.company}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="e.g. Acme Corp"
              />
            </div>

            <div className="pt-1">
              <LoadingButton
                loading={loading}
                text="Create company admin"
                loadingText="Creating..."
              />
            </div>
          </form>
        </div>

        {/* Admin list */}
        <div>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-sm font-semibold text-gray-800">Existing company admins</h2>
              <p className="mt-1 text-xs text-gray-500">
                Filter by company or search by name/email.
              </p>
            </div>
            <div className="text-xs text-gray-600">
              {displayAdmins.length} admin{displayAdmins.length === 1 ? "" : "s"}
            </div>
          </div>

          <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by name, email, or company..."
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 sm:max-w-xs"
            />
            <select
              value={companyFilter}
              onChange={(e) => setCompanyFilter(e.target.value)}
              className="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 sm:w-52"
            >
              <option value="ALL">All companies</option>
              {companies.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div className="mt-3 overflow-x-auto rounded-xl border border-gray-200">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                    Name
                  </th>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                    Email
                  </th>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                    Company
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {adminsLoading ? (
                  <tr>
                    <td colSpan={3} className="px-3 py-4 text-sm text-gray-600">
                      Loading company admins...
                    </td>
                  </tr>
                ) : adminsError ? (
                  <tr>
                    <td colSpan={3} className="px-3 py-4 text-sm text-gray-700">
                      Failed to load admins: {adminsError}
                    </td>
                  </tr>
                ) : displayAdmins.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="px-3 py-8 text-center text-sm text-gray-600">
                      No company admins found.
                    </td>
                  </tr>
                ) : (
                  displayAdmins.map((a) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="px-3 py-2 text-sm font-medium text-gray-900">
                        {a.fullName || "-"}
                      </td>
                      <td className="px-3 py-2 text-sm text-gray-700">{a.email || "-"}</td>
                      <td className="px-3 py-2 text-sm text-gray-700">
                        {a.company || "-"}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

