import { useState } from "react";
import { Link } from "react-router-dom";
import { requestForgotOtp } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";
import AuthCard from "../../components/auth/AuthCard";
import VerifyOtpModal from "../../components/auth/VerifyOtpModal";
import ResetPasswordModal from "../../components/auth/ResetPasswordModal";
import LoadingButton from "../../components/common/LoadingButton";
import bgImage from "../../assets/background.png";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const [showReset, setShowReset] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);

    try {
      await requestForgotOtp(email);
      showSuccess("OTP sent to your email");
      setShowOtp(true);
    } catch (err) {
      showError(
        err?.response?.data?.message || "Failed to send OTP"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="relative min-h-screen w-full bg-cover bg-center flex items-center justify-center px-4"
      style={{ backgroundImage: `url(${bgImage})` }}
    >
      <div className="absolute inset-0 bg-white/20" />

      {/* Forgot Password Card */}
      <div className="relative z-10">
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
              className="
                w-full
                rounded-xl
                border border-gray-300
                bg-white/40
                backdrop-blur
                px-4 py-2.5
                text-sm
                outline-none
                focus:ring-2 focus:ring-blue-500
              "
            />

            <LoadingButton
              loading={loading}
              text="Send OTP"
              loadingText="Sending OTP..."
            />
          </form>
        </AuthCard>
      </div>

      {/* OTP Modal */}
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

      {/* Reset Password Modal */}
      {showReset && (
        <ResetPasswordModal
          email={email}
          onClose={() => setShowReset(false)}
        />
      )}
    </div>
  );
}
