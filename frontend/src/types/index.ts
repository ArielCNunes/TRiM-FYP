// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  id: number;
  firstName: string;
  lastName: string;
  role: string;
  barberId?: number; // Present for BARBER role users
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: 'CUSTOMER' | 'BARBER' | 'ADMIN';
  createdAt: string;
}

// Service Types
export interface Service {
  id: number;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  active: boolean;
}

// Barber Types
export interface BarberUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export interface Barber {
  id: number;
  user: BarberUser;
  bio?: string;
  profileImageUrl?: string;
  active: boolean;
}

// Booking Types
export interface BookingRequest {
  customerId: number;
  barberId: number;
  serviceId: number;
  bookingDate: string;
  startTime: string;
  paymentMethod: 'pay_online' | 'pay_in_shop';
  notes?: string;
}

export interface BookingResponse {
  id: number;
  customer: User;
  barber: Barber;
  service: Service;
  bookingDate: string;
  startTime: string;
  endTime: string;
  status: string;
  paymentStatus: string;
  notes?: string;
  createdAt: string;
}