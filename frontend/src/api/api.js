import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true,
});

// response interceptor (NO console logs)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    return Promise.reject(
      err?.response?.data?.message || "Something went wrong"
    );
  }
);

export default api;
