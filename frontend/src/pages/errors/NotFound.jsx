import { Link } from "react-router-dom";
import bgImage from "../../assets/landing.png";

export default function NotFound() {
  return (
    <div
      className="min-h-screen bg-cover bg-center flex items-center justify-center relative"
      style={{ backgroundImage: `url(${bgImage})` }}
    >
      
      <section className="pt-1">
        <div className="relative z-10 text-center max-w-md px-6 transform -translate-y-55">
          <h1 className="text-5xl font-bold text-gray-900">
            404
          </h1>

          <p className="mt-3 text-gray-600">
            Oops! The page you're looking for doesn't exist.
          </p>

          <Link to="/" className="inline-block mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            Back to Home
          </Link>
        </div>
      </section>
    </div>
  );
}
