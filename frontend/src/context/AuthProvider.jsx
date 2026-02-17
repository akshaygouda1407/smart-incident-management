import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";
import { jwtDecode } from "jwt-decode";
import { logoutApi } from "../api/authApi";

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(() => {
    if (!localStorage.getItem("token")) return null;
    try {
      return jwtDecode(localStorage.getItem("token"));
    } catch {
      return null;
    }
  });

  // Listen for storage changes (e.g., when token is set in another tab/component)
  useEffect(() => {
    const handleStorageChange = () => {
      const newToken = localStorage.getItem("token");
      if (newToken !== token) {
        setToken(newToken);
        if (newToken) {
          try {
            setUser(jwtDecode(newToken));
          } catch {
            setUser(null);
          }
        } else {
          setUser(null);
        }
      }
    };

    // Listen for custom auth update event (for same-window updates)
    const handleAuthUpdate = (e) => {
      const newToken = e.detail?.token || localStorage.getItem("token");
      if (newToken) {
        setToken(newToken);
        try {
          setUser(jwtDecode(newToken));
        } catch {
          setUser(null);
        }
      }
    };

    window.addEventListener("storage", handleStorageChange);
    window.addEventListener("authUpdate", handleAuthUpdate);
    
    return () => {
      window.removeEventListener("storage", handleStorageChange);
      window.removeEventListener("authUpdate", handleAuthUpdate);
    };
  }, [token]);

  // Method to update auth state after login
  const updateAuth = (newToken) => {
    localStorage.setItem("token", newToken);
    setToken(newToken);
    try {
      const decoded = jwtDecode(newToken);
      setUser(decoded);
      // Dispatch custom event for other listeners
      window.dispatchEvent(new CustomEvent("authUpdate", { detail: { token: newToken } }));
    } catch {
      setUser(null);
    }
  };

  const logout = async () => {
    try {
      if (token) {
        await logoutApi(token);
      }
    } catch (err) {
      // Even if API fails, force logout
      console.warn("Logout API failed, clearing session");
      return err;
    } finally {
      localStorage.clear();
      setToken(null);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{ token, user, logout, updateAuth }}
    >
      {children}
    </AuthContext.Provider>
  );
};
