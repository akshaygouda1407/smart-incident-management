import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthCard from "../../components/auth/AuthCard";
import { register } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";
import VerifyOtpModal from "../../components/auth/VerifyOtpModal";
import bgImage from "../../assets/background.png";
import LoadingButton from "../../components/common/LoadingButton";

export default function Register() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const [otpEmail, setOtpEmail] = useState("");

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "USER",
  });

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);

    try {
      await register(form);

      showSuccess("OTP sent to your email");

      setOtpEmail(form.email);
      setShowOtp(true);
    } catch (err) {
      showError(
        err?.response?.data?.message || "Registration failed"
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
      
      {/* Card */}
      <div className="relative z-10">
        <AuthCard
          title="Create your account"
          subtitle="Start managing incidents with ServicePlus"
          footer={
            <>
              Already have an account?{" "}
              <Link to="/login" className="text-blue-600 font-medium">
                Login
              </Link>
            </>
          }
        >
          <form onSubmit={handleSubmit} className="space-y-4">
            <input
              name="fullName"
              placeholder="Full Name"
              required
              onChange={handleChange}
              className="w-full rounded-xl border border-gray-300 bg-white/40 backdrop-blur px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />

            <input
              name="email"
              type="email"
              placeholder="Email Address"
              required
              onChange={handleChange}
              className="w-full rounded-xl border border-gray-300 bg-white/40 backdrop-blur px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />

            <input
              name="password"
              type="password"
              placeholder="Password"
              required
              onChange={handleChange}
              className="w-full rounded-xl border border-gray-300 bg-white/40 backdrop-blur px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />

            <LoadingButton
              loading={loading}
              text="Register"
              loadingText="Creating account..."
            />
          </form>
        </AuthCard>
      </div>

      {/* OTP Modal */}
      {showOtp && (
        <VerifyOtpModal
          email={otpEmail}
          purpose="REGISTER"
          onClose={(success) => {
            setShowOtp(false);
            if (success) navigate("/login");
          }}
        />
      )}
    </div>
  );
}
