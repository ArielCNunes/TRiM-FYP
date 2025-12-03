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
  role: "CUSTOMER" | "BARBER" | "ADMIN";
  createdAt: string;
}

// Password Reset Types
export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface PasswordResetResponse {
  message: string;
}

// Service Category Types
export interface ServiceCategory {
  id: number;
  name: string;
  active: boolean;
}

export interface CategoryWithServices {
  id: number;
  name: string;
  active: boolean;
  services: Service[];
}

// Service Types
export interface Service {
  id: number;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  depositPercentage: number;
  active: boolean;
  categoryId?: number;
  categoryName?: string;
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

// Barber Break Types
export interface BarberBreak {
  id: number;
  barberId: number;
  startTime: string;
  endTime: string;
  label?: string;
}

// Booking Types
export interface BookingRequest {
  customerId: number;
  barberId: number;
  serviceId: number;
  bookingDate: string;
  startTime: string;
  paymentMethod: "pay_online" | "pay_in_shop";
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
  depositAmount?: number;
  outstandingBalance?: number;
  notes?: string;
  createdAt: string;
}

// Dashboard Types
export interface DashboardStats {
  totalBookings: number;
  todaysBookings: number;
  upcomingBookings: number;
  totalRevenue: number;
  thisMonthRevenue: number;
  activeCustomers: number;
  activeBarbers: number;
  popularServices: Array<{
    name: string;
    count: number;
  }>;
  recentBookings: Array<{
    customerName: string;
    barberName: string;
    serviceName: string;
    date: string;
    time: string;
    status: string;
  }>;
}
