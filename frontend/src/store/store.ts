import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';

// Configure the Redux store with the auth slice reducer.
export const store = configureStore({
  reducer: {
    auth: authReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;