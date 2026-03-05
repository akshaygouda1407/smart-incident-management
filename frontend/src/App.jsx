import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { subscribe, removeToast } from "./utils/toast";
import Toast from "./components/ui/Toast";
import AppRoutes from "./routes/AppRoutes";
import { useTheme } from "./context/useTheme";

function App() {
  const [toasts, setToasts] = useState([]);
  const { theme } = useTheme();
  const location = useLocation();

  const isAuthLightRoute =
    location.pathname === "/login" ||
    location.pathname === "/register" ||
    location.pathname === "/authentication/register" ||
    location.pathname === "/forgot-password" ||
    location.pathname === "/reset-password";

  useEffect(() => {
    return subscribe(setToasts);
  }, []);

  return (
    <>
      <AppRoutes />

      {/* TOAST CONTAINER */}
      <div
        style={{
          position: "fixed",
          top: 20,
          right: 20,
          zIndex: 99999,
          display: "flex",
          flexDirection: "column",
          gap: "12px",
          pointerEvents: "none"
        }}
      >
        {toasts.map((toast) => (
          <div key={toast.id} style={{ pointerEvents: "auto" }}>
            <Toast
              toast={toast}
              onClose={removeToast}
              theme={isAuthLightRoute ? "light" : theme}
            />
          </div>
        ))}
      </div>
    </>
  );
}

export default App;
