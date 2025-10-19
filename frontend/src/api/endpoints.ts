import api from './axios';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User,
} from '../types';

/**
 * Authentication API Endpoints
 * 
 * Provides methods for user authentication and registration.
 * All endpoints return Axios promises with typed responses.
 */
export const authApi = {
  /**
   * Login endpoint
   * 
   * Authenticates a user with email and password credentials.
   * 
   * @param credentials - User login credentials (email and password)
   * @returns Promise resolving to LoginResponse containing token and user info
   */
  login: (credentials: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', credentials),
  
  /**
   * Register endpoint
   * 
   * Creates a new user account with provided registration details.
   * 
   * @param userData - User registration data (email, password, firstName, lastName, etc.)
   * @returns Promise resolving to User object with created user details
   */
  register: (userData: RegisterRequest) =>
    api.post<User>('/auth/register', userData),
};