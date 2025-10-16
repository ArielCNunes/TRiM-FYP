import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

// Represents the authenticated user state that the rest of the app can query.
interface AuthState {
  token: string | null;
  user: {
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  } | null;
  isAuthenticated: boolean;
}

// Restore the persisted auth session, if any, so a page refresh does not log users out.
const loadAuthState = (): AuthState => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
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
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{ token: string; email: string; firstName: string; lastName: string; role: string }>
    ) => {
      // Sync Redux auth state with the fresh credentials received from a successful login.
      const { token, email, firstName, lastName, role } = action.payload;
      state.token = token;
      state.user = { email, firstName, lastName, role };
      state.isAuthenticated = true;
      
      // Persist to localStorage so the session can be restored on the next visit.
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ email, firstName, lastName, role }));
    },
    
    logout: (state) => {
      // Drop all auth state, effectively logging out the current user.
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      
      // Remove persisted credentials to avoid restoring an invalid session.
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;