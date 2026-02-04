import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { resetPassword } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";
import AuthCard from "../../components/auth/AuthCard";

export default function ResetPassword() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const email = state?.email;

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password.length < 6) {
      showError("Password must be at least 6 characters");
      return;
    }

    if (password !== confirmPassword) {
      showError("Passwords do not match");
      return;
    }

    try {
      setLoading(true);
      await resetPassword(email, password);
      showSuccess("Password updated successfully");
      navigate("/login");
    } catch {
      showError("Failed to reset password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthCard
      title="Reset your password"
      subtitle="Choose a strong new password to secure your account"
      footer={
        <Link to="/login" className="text-blue-600 font-medium">
          Back to Login
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="password"
          placeholder="New password"
          required
          onChange={(e) => setPassword(e.target.value)}
          className="w-full rounded-lg border px-3 py-2 text-sm
                     focus:ring-2 focus:ring-blue-500 outline-none"
        />

        <input
          type="password"
          placeholder="Confirm new password"
          required
          onChange={(e) => setConfirmPassword(e.target.value)}
          className="w-full rounded-lg border px-3 py-2 text-sm
                     focus:ring-2 focus:ring-blue-500 outline-none"
        />

        <button
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded-lg
                     text-sm font-medium hover:bg-blue-700 transition
                     disabled:opacity-60"
        >
          {loading ? "Updating..." : "Reset Password"}
        </button>
      </form>
    </AuthCard>
  );
}
