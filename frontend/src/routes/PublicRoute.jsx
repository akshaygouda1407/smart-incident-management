import { Navigate } from "react-router-dom";
import { isAuthenticated } from "../auth/auth";

export default function PublicRoute({ children }) {
  if (isAuthenticated()) {
    const role = localStorage.getItem("userRole");

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
