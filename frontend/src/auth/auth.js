export const getToken = () => {
  return localStorage.getItem("accessToken");
};

export const isAuthenticated = () => {
  return !!getToken();
};

export const logout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userRole");
};