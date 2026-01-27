import axios from "axios";

/**
 * Extract business slug from subdomain or fallback to default
 * Examples:
 *   "v7.localhost" → "v7"
 *   "shop2.trim.com" → "shop2"
 *   "localhost" → "v7-barbers" (default for development)
 */
const getBusinessSlug = (): string => {
  const hostname = window.location.hostname;
  const parts = hostname.split(".");

  // If there's a subdomain and it's not a common non-business subdomain
  if (parts.length >= 2) {
    const subdomain = parts[0].toLowerCase();
    const ignoredSubdomains = ["localhost", "www", "api", "app"];
    if (!ignoredSubdomains.includes(subdomain)) {
      return subdomain;
    }
  }

  // Check for query parameter fallback
  const urlParams = new URLSearchParams(window.location.search);
  const businessParam = urlParams.get("business");
  if (businessParam) {
    return businessParam;
  }

  // Default for development - change this to your business slug
  return "v7-barbers";
};

const api = axios.create({
  baseURL: "http://localhost:8080/api", // Base URL for backend API
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Add JWT token and business slug to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Always send business slug header
    config.headers["X-Business-Slug"] = getBusinessSlug();

    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// Response interceptor - Handle 401 errors (expired token)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on 401 if it's NOT an auth endpoint (login, register, etc.)
    const isAuthEndpoint = error.config?.url?.startsWith("/auth");
    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/auth"; // Redirect to auth page
    }
    return Promise.reject(error);
  },
);

export default api;
