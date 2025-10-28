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

// Restore the persisted auth session, if any, so a page refresh does not log users out.
const loadAuthState = (): AuthState => {
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
      }>
    ) => {
      const { token, email, firstName, lastName, role, id, barberId } =
        action.payload;
      state.token = token;
      state.user = { id, email, firstName, lastName, role, barberId };
      state.isAuthenticated = true;

      localStorage.setItem("token", token);
      localStorage.setItem(
        "user",
        JSON.stringify({ id, email, firstName, lastName, role, barberId })
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
