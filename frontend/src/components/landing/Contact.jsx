import { useState } from "react";
import { showSuccess } from "../../utils/toast";
import LoadingButton from "../common/LoadingButton";

export default function Contact() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    message: "",
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    showSuccess("Thanks for reaching out! We’ll contact you soon.");
    setForm({ name: "", email: "", message: "" });
  };

  return (
    <section
      id="contact"
      className="relative py-32 px-6 bg-gradient-to-br from-blue-50 via-white to-indigo-50 overflow-hidden"
    >
      {/* Decorative glow */}
      <div className="absolute -top-32 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-indigo-200/30 rounded-full blur-3xl" />

      <div className="relative max-w-3xl mx-auto">
        <h2 className="text-4xl font-bold text-gray-900 text-center">
          Contact Us
        </h2>

        <p className="mt-4 text-center text-gray-600">
          Have questions or want a demo? We'd love to hear from you.
        </p>

        <form
          onSubmit={handleSubmit}
          className="mt-14 bg-white/90 backdrop-blur p-10 rounded-2xl shadow-xl border border-gray-200"
        >
          {/* Name */}
          <div className="mb-5">
            <label className="block text-sm font-medium text-gray-700">
              Full Name
            </label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
              className="mt-2 w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Email */}
          <div className="mb-5">
            <label className="block text-sm font-medium text-gray-700">
              Email Address
            </label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              required
              className="mt-2 w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Message */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700">
              Message
            </label>
            <textarea
              name="message"
              rows="4"
              value={form.message}
              onChange={handleChange}
              required
              className="mt-2 w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`
              inline-flex items-center justify-center gap-2
              px-8 py-3 rounded-lg font-medium
              transition-all duration-200
              ${loading
                ? "bg-blue-400 text-white cursor-not-allowed"
                : "border border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white"}
            `}
          >
            {loading && (
              <span className="h-4 w-4 border-2 border-white/60 border-t-white rounded-full animate-spin" />
            )}
            {loading ? "Sending..." : "Send Message"}
          </button>


        </form>
      </div>
    </section>
  );
}
