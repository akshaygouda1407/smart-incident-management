import { useEffect, useState } from "react";
import { getAllAuditLogs } from "../../api/auditLogApi";
import { showError } from "../../utils/toast";

function getApiMessage(err) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.statusMessage ||
    err?.message ||
    "Something went wrong"
  );
}

export default function SuperAdminLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchLogs = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await getAllAuditLogs();
        const data = res?.data ?? res;
        setLogs(Array.isArray(data) ? data : []);
      } catch (err) {
        const msg = getApiMessage(err);
        setError(msg);
        showError(msg);
      } finally {
        setLoading(false);
      }
    };

    fetchLogs();
  }, []);

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Super Admin · Audit Logs</h1>
          <p className="mt-1 text-sm text-gray-600">
            System-wide audit trail of important actions.
          </p>
        </div>
        <div className="text-sm text-gray-600">
          {logs.length} entr{logs.length === 1 ? "y" : "ies"}
        </div>
      </div>

      <div className="mt-5 overflow-x-auto rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Time
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Actor
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Role
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Action
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Entity
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                Description
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {loading ? (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-sm text-gray-600">
                  Loading logs...
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-sm text-gray-700">
                  Failed to load logs: {error}
                </td>
              </tr>
            ) : logs.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-sm text-gray-600">
                  No audit logs found.
                </td>
              </tr>
            ) : (
              logs
                .slice()
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-4 py-2 text-xs text-gray-700 whitespace-nowrap">
                      {log.timestamp
                        ? new Date(log.timestamp).toLocaleString()
                        : "-"}
                    </td>
                    <td className="px-4 py-2 text-sm text-gray-800">
                      {log.actorEmail || "-"}
                    </td>
                    <td className="px-4 py-2 text-xs text-gray-700 uppercase">
                      {log.actorRole || "-"}
                    </td>
                    <td className="px-4 py-2 text-xs font-semibold text-gray-800">
                      {log.action || "-"}
                    </td>
                    <td className="px-4 py-2 text-xs text-gray-700">
                      {log.entityType || "-"}
                      {log.entityId != null && ` #${log.entityId}`}
                    </td>
                    <td className="px-4 py-2 text-xs text-gray-700 max-w-xs">
                      <div className="line-clamp-2">{log.description || "-"}</div>
                    </td>
                  </tr>
                ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

