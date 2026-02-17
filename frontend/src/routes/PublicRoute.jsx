import { Navigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

export default function PublicRoute({ children }) {
  const { token, user } = useAuth();

  // If user is authenticated, redirect to their dashboard
  if (token && user) {
    if (user?.mustChangePassword) {
      return <Navigate to="/force-change-password" replace />;
    }

    const role = user.role;

    switch (role) {
      case "ADMIN":
        return <Navigate to="/admin/dashboard" replace />;
      case "MANAGER":
        return <Navigate to="/manager/dashboard" replace />;
      case "ENGINEER":
        return <Navigate to="/engineer/dashboard" replace />;
      default:
        return <Navigate to="/user/dashboard" replace />;
    }
  }

  return children;
}
