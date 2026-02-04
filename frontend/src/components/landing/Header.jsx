import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

export default function Header() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header className="fixed top-0 left-0 w-full z-50">
      <div
        className={`backdrop-blur-md bg-white/80 transition ${
          scrolled ? "shadow-md border-b border-gray-200" : ""
        }`}
      >
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">
            Service<span className="text-blue-600">Plus</span>
          </h1>

          <nav className="hidden md:flex gap-8 text-md font-medium text-gray-700">
            <a href="#features" className="hover:text-blue-600">Features</a>
            <a href="#workflow" className="hover:text-blue-600">Workflow</a>
            <a href="#contact" className="hover:text-blue-600">Contact Us</a>
          </nav>

          <Link
            to="/login"
            className="px-4 py-1.5 text-sm rounded-md border border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white transition"
          >
            Login
          </Link>
        </div>
      </div>
    </header>
  );
}
