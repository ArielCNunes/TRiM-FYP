import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

// Represents the authenticated user state that the rest of the app can query.
interface AuthState {
  token: string | null;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    barberId?: number;
  } | null;
  isAuthenticated: boolean;
}

/**
 * Check URL params for auth token (used for cross-subdomain redirect).
 * If found, store in localStorage and clean up URL.
 */
const extractAuthFromUrl = (): {
  token: string;
  user: AuthState["user"];
} | null => {
  const params = new URLSearchParams(window.location.search);
  const authToken = params.get("authToken");
  const authUserStr = params.get("authUser");

  if (authToken && authUserStr) {
    try {
      const user = JSON.parse(authUserStr);

      // Store in localStorage for persistence
      localStorage.setItem("token", authToken);
      localStorage.setItem("user", authUserStr);

      // Clean up URL by removing auth params
      params.delete("authToken");
      params.delete("authUser");
      const newUrl = params.toString()
        ? `${window.location.pathname}?${params.toString()}`
        : window.location.pathname;
      window.history.replaceState({}, "", newUrl);

      return { token: authToken, user };
    } catch {
      // Invalid JSON, ignore
    }
  }

  return null;
};

// Restore the persisted auth session, if any, so a page refresh does not log users out.
const loadAuthState = (): AuthState => {
  // First check URL for auth token (cross-subdomain redirect)
  const urlAuth = extractAuthFromUrl();
  if (urlAuth) {
    return {
      token: urlAuth.token,
      user: urlAuth.user,
      isAuthenticated: true,
    };
  }

  // Fall back to localStorage
  const token = localStorage.getItem("token");
  const userStr = localStorage.getItem("user");

  if (token && userStr) {
    return {
      token,
      user: JSON.parse(userStr),
      isAuthenticated: true,
    };
  }

  return {
    token: null,
    user: null,
    isAuthenticated: false,
  };
};

const initialState: AuthState = loadAuthState();

// The auth slice manages login state and user info, and persists it to localStorage.
const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{
        id: number;
        token: string;
        email: string;
        firstName: string;
        lastName: string;
        role: string;
        barberId?: number;
      }>,
    ) => {
      const { token, email, firstName, lastName, role, id, barberId } =
        action.payload;
      state.token = token;
      state.user = { id, email, firstName, lastName, role, barberId };
      state.isAuthenticated = true;

      localStorage.setItem("token", token);
      localStorage.setItem(
        "user",
        JSON.stringify({ id, email, firstName, lastName, role, barberId }),
      );
    },

    logout: (state) => {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
