import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import { authApi } from "../../api/endpoints";

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
 * Async thunk to handle secure token exchange from URL.
 * Exchanges temporary token for actual JWT credentials.
 */
export const exchangeTokenFromUrl = createAsyncThunk(
  "auth/exchangeTokenFromUrl",
  async (_, { rejectWithValue }) => {
    const params = new URLSearchParams(window.location.search);
    const exchangeToken = params.get("exchangeToken");

    if (!exchangeToken) {
      return rejectWithValue("No exchange token in URL");
    }

    try {
      const response = await authApi.exchangeToken(exchangeToken);
      const { token, user: userJson } = response.data;
      const user = JSON.parse(userJson);

      // Store in localStorage
      localStorage.setItem("token", token);
      localStorage.setItem("user", userJson);

      // Clean URL (remove exchange token from browser history)
      window.history.replaceState({}, document.title, window.location.pathname);

      return { token, user };
    } catch (error) {
      // Clean URL even on failure
      window.history.replaceState({}, document.title, window.location.pathname);
      throw error;
    }
  },
);

// Restore the persisted auth session, if any, so a page refresh does not log users out.
const loadAuthState = (): AuthState => {
  // Load from localStorage (exchange token is handled async via thunk)
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
  extraReducers: (builder) => {
    builder
      .addCase(exchangeTokenFromUrl.fulfilled, (state, action) => {
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.isAuthenticated = true;
      })
      .addCase(exchangeTokenFromUrl.rejected, (state) => {
        // Token exchange failed - user needs to login manually
        // Don't change state, just let them proceed unauthenticated
      });
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
