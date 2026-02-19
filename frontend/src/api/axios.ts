import axios from "axios";

/**
 * Extract business slug from subdomain or fallback to default
 * Examples:
 *   "v7.localhost" → "v7"
 *   "shop2.trim.com" → "shop2"
 *   "localhost" → "v7-barbers" (default for development)
 */
export const getBusinessSlug = (): string => {
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

/**
 * Build API base URL preserving the current subdomain for multi-tenancy
 * Examples:
 *   "http://v1.localhost:3000" → "http://v1.localhost:8080/api"
 */
const getApiBaseUrl = (): string => {
  const { protocol, hostname } = window.location;

  // In production, API might be on same domain via proxy
  // In development, use port 8080
  const appDomain = import.meta.env.VITE_APP_DOMAIN || "localhost";
  const isDevelopment = hostname.includes(appDomain);
  const apiHost = isDevelopment ? `${hostname}:8080` : hostname;

  return `${protocol}//${apiHost}/api`;
};

const api = axios.create({
  baseURL: getApiBaseUrl(),
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

// Response interceptor - Handle 401 and 403 errors (expired token or tenant mismatch)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on 401/403 if it's not an auth endpoint
    const isAuthEndpoint = error.config?.url?.startsWith("/auth");
    const status = error.response?.status;

    if ((status === 401 || status === 403) && !isAuthEndpoint) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      localStorage.removeItem("businessSlug");
      window.location.href = "/auth"; // Redirect to auth page
    }
    return Promise.reject(error);
  },
);

export default api;
