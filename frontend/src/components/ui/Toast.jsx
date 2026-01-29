import { useRef } from "react";
import { X, CheckCircle, XCircle, AlertTriangle, Info } from "lucide-react";
import "./toast.css";

const ICONS = {
  success: <CheckCircle size={20} />,
  error: <XCircle size={20} />,
  warning: <AlertTriangle size={20} />,
  info: <Info size={20} />
};

export default function Toast({ toast, onClose, theme }) {
  const timerRef = useRef(null);

  const pauseTimer = () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  const resumeTimer = () => {
    pauseTimer();
    timerRef.current = setTimeout(
      () => onClose(toast.id),
      toast.duration
    );
  };

  return (
    <div
      className={`toast toast-${toast.type} ${theme}`}
      onMouseEnter={pauseTimer}
      onMouseLeave={resumeTimer}
    >
      <div className="toast-left">
        <span className="toast-icon">{ICONS[toast.type]}</span>
        <div className="toast-text">
          <strong className="toast-title">
            {toast.type.toUpperCase()}
          </strong>
          <span className="toast-message">{toast.message}</span>
        </div>
      </div>

      <button className="toast-close" onClick={() => onClose(toast.id)}>
        <X size={16} />
      </button>

      <div className={`toast-progress ${toast.type}`} />
    </div>
  );
}
