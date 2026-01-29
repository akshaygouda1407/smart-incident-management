import { useState } from "react";
import { resetPassword } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";

export default function ResetPasswordModal({ email, onClose }) {
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (password.length < 6) {
      showError("Password must be at least 6 characters");
      return;
    }

    setLoading(true);
    try {
      await resetPassword(email, password);
      showSuccess("Password reset successfully 🔐");
      onClose(true);
    } catch (err) {
      showError(err?.response?.data?.message || "Reset failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="otp-overlay">
      <div className="otp-card">
        <h2 className="text-lg font-semibold">Reset Password</h2>
        <p className="text-sm text-gray-600 mt-1">
          Set a new password for <b>{email}</b>
        </p>

        <input
          type="password"
          placeholder="New password"
          className="w-full border rounded-lg px-3 py-2 mt-4"
          onChange={(e) => setPassword(e.target.value)}
        />

        <button
          onClick={handleSubmit}
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded-lg mt-4"
        >
          {loading ? "Saving..." : "Reset Password"}
        </button>

        <button
          onClick={() => onClose(false)}
          className="mt-2 text-sm text-gray-500"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
