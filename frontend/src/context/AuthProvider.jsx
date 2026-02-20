import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";
import { jwtDecode } from "jwt-decode";
import { logoutApi } from "../api/authApi";
import { getUserById } from "../api/userApi";

function mergeFullName(decoded, fullName) {
  if (!decoded) return decoded;
  if (!fullName) return decoded;
  return { ...decoded, fullName };
}

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

  // Enrich decoded JWT user with profile data (e.g., fullName)
  useEffect(() => {
    let cancelled = false;

    const hydrateProfile = async () => {
      if (!token) return;

      let decoded;
      try {
        decoded = jwtDecode(token);
      } catch {
        return;
      }

      const userId = decoded?.userId;
      if (!userId) return;

      try {
        const res = await getUserById(userId);
        const fullName = res?.data?.fullName;
        if (cancelled) return;
        if (fullName) {
          setUser((prev) => mergeFullName(prev || decoded, fullName));
        } else {
          // At least ensure latest decoded is set
          setUser((prev) => prev || decoded);
        }
      } catch {
        // If profile fetch fails, still ensure we have decoded data
        if (cancelled) return;
        setUser((prev) => prev || decoded);
      }
    };

    hydrateProfile();
    return () => {
      cancelled = true;
    };
  }, [token]);

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
        await logoutApi();
      }
    } catch {
      // Even if API fails, force logout
      // Silently handle logout errors - user will be logged out locally anyway
    } finally {
      localStorage.removeItem("token");
      localStorage.removeItem("role");
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
