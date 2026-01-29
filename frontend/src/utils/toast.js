let listeners = [];
let toasts = [];
let id = 0;

function notify() {
  listeners.forEach((l) => l([...toasts]));
}

export function subscribe(listener) {
  listeners.push(listener);
  listener([...toasts]);

  return () => {
    listeners = listeners.filter((l) => l !== listener);
  };
}

function addToast(type, message, duration = 4000) {
  const toast = { id: ++id, type, message, duration };
  toasts.push(toast);
  notify();

  setTimeout(() => removeToast(toast.id), duration);
}

export function removeToast(toastId) {
  toasts = toasts.filter((t) => t.id !== toastId);
  notify();
}

export const showSuccess = (msg) => addToast("success", msg);
export const showError   = (msg) => addToast("error", msg);
export const showWarning = (msg) => addToast("warning", msg);
export const showInfo    = (msg) => addToast("info", msg);