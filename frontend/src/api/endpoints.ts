import api from './axios';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User,
  Service,
  Barber,
  BookingRequest,
  BookingResponse
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

/**
 * Services API Endpoints
 * 
 * Provides methods for retrieving service offerings.
 */
export const servicesApi = {
  /**
   * Get active services
   * 
   * Retrieves all currently active services available for booking.
   * 
   * @returns Promise resolving to an array of Service objects
   */
  getActive: () =>
    api.get<Service[]>('/services/active'),

  /**
   * Create a new service
   * 
   * Creates a new service offering with the specified details.
   * 
   * @param service - The service data including name, description, duration, and price
   * @returns Promise resolving to the created Service object
   */
  create: (service: any) =>
    api.post<Service>('/services', service),
};

/**
 * Barbers API Endpoints
 * 
 * Provides methods for retrieving barber information.
 */
export const barbersApi = {
  /**
   * Get active barbers
   * 
   * Retrieves all currently active barbers available for bookings.
   * 
   * @returns Promise resolving to an array of Barber objects
   */
  getActive: () =>
    api.get<Barber[]>('/barbers/active'),

  /**
   * Create a new barber
   * 
   * Creates a new barber account with the specified user credentials.
   * 
   * @param barber - The barber data including name, email, phone, and bio
   * @returns Promise resolving to the created Barber object
   */
  create: (barber: any) =>
    api.post<Barber>('/barbers', barber),
};

/**
 * Availability API Endpoints
 * 
 * Provides methods for checking barber availability and time slots.
 */
export const availabilityApi = {
  /**
   * Get available time slots
   * 
   * Retrieves all available time slots for a specific barber on a given date
   * for a particular service. Used in the booking flow to display available
   * appointment times to customers.
   * 
   * @param barberId - The unique identifier of the barber
   * @param date - The date to check availability
   * @param serviceId - The unique identifier of the requested service
   * @returns Promise resolving to an array of time slot strings
   */
  getSlots: (barberId: number, date: string, serviceId: number) =>
    api.get<string[]>(`/availability?barberId=${barberId}&date=${date}&serviceId=${serviceId}`),
};

/**
 * Bookings API Endpoints
 * 
 * Provides methods for creating and managing bookings.
 */
export const bookingsApi = {
  /**
   * Create a new booking
   * 
   * Creates a new appointment booking with the specified details.
   * 
   * @param booking - The booking request data including barber, service, date, and time
   * @returns Promise resolving to BookingResponse with confirmation details
   */
  create: (booking: BookingRequest) =>
    api.post<BookingResponse>('/bookings', booking),
};