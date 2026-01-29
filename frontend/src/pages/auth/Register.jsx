import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthCard from "../../components/auth/AuthCard";
import { register, requestRegisterOtp } from "../../api/authApi";
import { showError, showSuccess } from "../../utils/toast";
import VerifyOtpModal from "../../components/auth/VerifyOtpModal";
// import ResetPasswordModal from "../../components/auth/ResetPasswordModal";

export default function Register() {
  const navigate = useNavigate();

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

    try {
      await register(form);
      await requestRegisterOtp(form.email);

      showSuccess("OTP sent to your email 📩");

      setOtpEmail(form.email);
      setShowOtp(true); // 🔥 show modal

    } catch (err) {
      showError(err?.response?.data?.message || "Registration failed");
    }
  };

  return (
    <>
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
            className="w-full rounded-lg border px-3 py-2 text-sm"
          />

          <input
            name="email"
            type="email"
            placeholder="Email Address"
            required
            onChange={handleChange}
            className="w-full rounded-lg border px-3 py-2 text-sm"
          />

          <input
            name="password"
            type="password"
            placeholder="Password"
            required
            onChange={handleChange}
            className="w-full rounded-lg border px-3 py-2 text-sm"
          />

          <button className="w-full bg-blue-600 text-white py-2 rounded-lg">
            Register
          </button>
        </form>
      </AuthCard>

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
    </>
  );
}
