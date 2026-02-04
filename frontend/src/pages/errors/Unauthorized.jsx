import { Link } from "react-router-dom";
import bgImage from "../../assets/landing.png";

export default function LandingPage() {
  return (
    <div
      className="min-h-screen w-full bg-cover bg-center flex items-center justify-center px-4"
      style={{ backgroundImage: `url(${bgImage})` }}
    >
      <section className="pt-1">
        <div className="relative z-10 text-center max-w-md px-6 transform -translate-y-55">
          <h1 className="text-4xl font-bold text-gray-900">
            Access Denied
          </h1>

          <p className="mt-3 text-gray-600">
            You don't have permission to access this page.
          </p>
          <Link to="/login" className="inline-block mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            Go to Login
          </Link>
        </div>
      </section>
    </div>
  );
}