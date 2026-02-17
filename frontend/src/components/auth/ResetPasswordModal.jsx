import { useState } from "react";
import { resetPassword } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";

export default function ResetPasswordModal({ email, onClose }) {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  /* ---------------- Password Match ---------------- */
  const passwordsMatch =
    confirmPassword.length > 0 && password === confirmPassword;

  const passwordsMismatch =
    confirmPassword.length > 0 && password !== confirmPassword;

  /* ---------------- Password Strength ---------------- */
  const getStrength = () => {
    let score = 0;
    if (password.length >= 6) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 1)
      return { label: "Weak", color: "bg-red-500", width: "w-1/4" };
    if (score === 2)
      return { label: "Fair", color: "bg-yellow-500", width: "w-2/4" };
    if (score === 3)
      return { label: "Good", color: "bg-blue-500", width: "w-3/4" };
    return { label: "Strong", color: "bg-green-500", width: "w-full" };
  };

  const strength = getStrength();

  /* ---------------- Submit ---------------- */
  const handleSubmit = async () => {
    if (loading) return;

    if (password.length < 6) {
      showError("Password must be at least 6 characters");
      return;
    }

    if (password !== confirmPassword) {
      showError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      await resetPassword(email, password);
      showSuccess("Password reset successfully");
      onClose(true);
    } catch (err) {
      showError(err?.response?.data?.message || "Reset failed");
    } finally {
      setLoading(false);
    }
  };

  /* ---------------- UI ---------------- */
  return (
    <div className="otp-overlay">
      <div className="otp-card">
        <h2 className="text-lg font-semibold">Reset Password</h2>
        <p className="text-sm text-gray-600 mt-1">
          Set a new password for <b>{email}</b>
        </p>

        {/* New Password */}
        <div className="relative mt-4">
          <input
            type={showPassword ? "text" : "password"}
            placeholder="New password"
            value={password}
            disabled={loading}
            onChange={(e) => setPassword(e.target.value)}
            className={`
              w-full rounded-lg px-3 py-2 pr-10 outline-none focus:ring-2
              ${
                passwordsMismatch
                  ? "border border-red-500 focus:ring-red-400"
                  : passwordsMatch
                  ? "border border-green-500 focus:ring-green-400"
                  : "border border-gray-300 focus:ring-blue-500"
              }
            `}
          />

          {/* Eye */}
          <span
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-gray-400 hover:text-gray-600"
          >
            {showPassword ? (
              /* Eye Off */
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M3 3l18 18M10.73 5.08A10.43 10.43 0 0112 5c4.48 0 8.27 2.94 9.54 7a10.42 10.42 0 01-4.1 5.14M6.1 6.1A10.42 10.42 0 002.46 12c1.27 4.06 5.06 7 9.54 7"
                />
              </svg>
            ) : (
              /* Eye */
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7"
                />
              </svg>
            )}
          </span>
        </div>

        {/* Strength Meter */}
        {password && (
          <div className="mt-2">
            <div className="h-2 w-full bg-gray-200 rounded">
              <div className={`h-2 rounded ${strength.color} ${strength.width}`} />
            </div>
            <p className="text-xs mt-1 text-gray-600">
              Strength: <b>{strength.label}</b>
            </p>
          </div>
        )}

        {/* Confirm Password */}
        <div className="relative mt-4">
          <input
            type={showConfirmPassword ? "text" : "password"}
            placeholder="Confirm password"
            value={confirmPassword}
            disabled={loading}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className={`
              w-full rounded-lg px-3 py-2 pr-10 outline-none focus:ring-2
              ${
                passwordsMismatch
                  ? "border border-red-500 focus:ring-red-400"
                  : passwordsMatch
                  ? "border border-green-500 focus:ring-green-400"
                  : "border border-gray-300 focus:ring-blue-500"
              }
            `}
          />

          {/* Eye */}
          <span
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-gray-400 hover:text-gray-600"
          >
            {showConfirmPassword ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M3 3l18 18M10.73 5.08A10.43 10.43 0 0112 5c4.48 0 8.27 2.94 9.54 7a10.42 10.42 0 01-4.1 5.14M6.1 6.1A10.42 10.42 0 002.46 12c1.27 4.06 5.06 7 9.54 7"
                />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7"
                />
              </svg>
            )}
          </span>
        </div>

        {/* Match message */}
        {passwordsMismatch && (
          <p className="text-xs text-red-500 mt-1">
            Passwords do not match
          </p>
        )}
        {passwordsMatch && (
          <p className="text-xs text-green-600 mt-1">
            Passwords match
          </p>
        )}

        {/* Submit */}
        <button
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full flex items-center justify-center gap-2 py-2 rounded-lg mt-4 text-white transition
            ${loading ? "bg-blue-400 cursor-not-allowed" : "bg-blue-600 hover:bg-blue-700"}
          `}
        >
          {loading && (
            <span className="h-4 w-4 border-2 border-white/60 border-t-white rounded-full animate-spin" />
          )}
          {loading ? "Saving..." : "Reset Password"}
        </button>

        <button
          onClick={() => onClose(false)}
          disabled={loading}
          className="mt-2 text-sm text-gray-500"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
