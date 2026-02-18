import api from "./axios";
import axios from "axios";

// Base URL constant for consistency
const BASE_URL = "http://localhost:8080/api";

// Public endpoints (no auth token needed)
// These use direct axios to avoid response unwrapping from api instance
export const login = (email, password) => {
  return axios.post(`${BASE_URL}/auth/login`, {
    email,
    password
  });
};

export const register = (data) => {
  return axios.post(`${BASE_URL}/auth/register`, data);
};

export const requestRegisterOtp = (email) => {
  return axios.post(
    `${BASE_URL}/auth/register/request-otp`,
    null,
    { params: { email } }
  );
};

export const verifyRegisterOtp = (email, otp) => {
  return axios.post(`${BASE_URL}/auth/register/verify-otp`, {
    email,
    otp
  });
};

export const requestForgotOtp = (email) => {
  return axios.post(
    `${BASE_URL}/auth/forgot-password/request-otp`,
    null,
    { params: { email } }
  );
};

export const verifyForgotOtp = (email, otp) => {
  return axios.post(
    `${BASE_URL}/auth/forgot-password/verify-otp`,
    null,
    { params: { email, otp } }
  );
};

export const resetPassword = (email, newPassword) => {
  return axios.post(
    `${BASE_URL}/auth/forgot-password/reset`,
    null,
    { params: { email, newPassword } }
  );
};

export const sendContactForm = (data) => {
  return axios.post(`${BASE_URL}/contact/submit`, data);
};

// Authenticated endpoints (use api instance with auth interceptor)
export const getCurrentUser = async () => {
  return api.get("/me");
};

export const logoutApi = async () => {
  // Use api instance - token will be added by interceptor
  return api.post("/auth/logout");
};
