import { useState } from "react";
import { Link } from "react-router-dom";
import { requestForgotOtp } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";
import AuthCard from "../../components/auth/AuthCard";
import VerifyOtpModal from "../../components/auth/VerifyOtpModal";
import ResetPasswordModal from "../../components/auth/ResetPasswordModal";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [showOtp, setShowOtp] = useState(false);
  const [showReset, setShowReset] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await requestForgotOtp(email);
      showSuccess("OTP sent to your email");
      setShowOtp(true);
    } catch (err) {
      showError(err?.response?.data?.message || "Failed to send OTP");
    }
  };

  return (
    <>
      {/* FORGOT PASSWORD CARD */}
      <AuthCard
        title="Forgot your password?"
        subtitle="Enter your registered email to reset your password"
        footer={
          <Link to="/login" className="text-blue-600 font-medium">
            Back to Login
          </Link>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="email"
            placeholder="Registered email address"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border px-3 py-2 text-sm
                       focus:ring-2 focus:ring-blue-500 outline-none"
          />

          <button
            className="w-full bg-blue-600 text-white py-2 rounded-lg
                       text-sm font-medium hover:bg-blue-700 transition"
          >
            Send OTP
          </button>
        </form>
      </AuthCard>

      {/* OTP MODAL */}
      {showOtp && (
        <VerifyOtpModal
          email={email}
          purpose="FORGOT_PASSWORD"
          onClose={(verified) => {
            setShowOtp(false);
            if (verified) setShowReset(true);
          }}
        />
      )}

      {/* RESET PASSWORD MODAL */}
      {showReset && (
        <ResetPasswordModal
          email={email}
          onClose={() => setShowReset(false)}
        />
      )}
    </>
  );
}
