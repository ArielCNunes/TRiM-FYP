import axios from "axios";

/**
 * Extract business slug from subdomain or query param
 * Examples:
 *   "v7.localhost" → "v7"
 *   "shop2.trim.com" → "shop2"
 *   "localhost" → "" (no slug)
 */
export const getBusinessSlug = (): string => {
  const hostname = window.location.hostname;
  const parts = hostname.split(".");

  // For real domains (e.g., trimbooking.ie), we need 3+ parts to have a subdomain
  // (b.trimbooking.ie → ["b", "trimbooking", "ie"]).
  // For localhost (e.g., b.localhost), 2+ parts is enough.
  const isLocalhost = parts[parts.length - 1] === "localhost";
  const minParts = isLocalhost ? 2 : 3;

  if (parts.length >= minParts) {
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

  // No slug available
  return "";
};

/**
 * Build API base URL preserving the current subdomain for multi-tenancy
 */
const getApiBaseUrl = (): string => {
  // In Docker, nginx proxies /api to the backend.
  // In dev (vite), the proxy in vite.config.ts handles it
  return "/api";
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

    // Send business slug header only when present
    const businessSlug = getBusinessSlug();
    if (businessSlug) {
      config.headers["X-Business-Slug"] = businessSlug;
    }

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
