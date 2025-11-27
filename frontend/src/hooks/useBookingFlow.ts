import { useState, useEffect } from "react";
import {
  categoriesApi,
  barbersApi,
  availabilityApi,
  bookingsApi,
  paymentsApi,
  authApi,
} from "../api/endpoints";
import type {
  Service,
  Barber,
  BookingRequest,
  CategoryWithServices,
} from "../types";

/** Booking wizard steps: service → barber → datetime → customerinfo → confirmation → payment → saveaccount */
type Step =
  | "service"
  | "barber"
  | "datetime"
  | "customerinfo"
  | "confirmation"
  | "payment"
  | "saveaccount";

interface CustomerInfo {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

/**
 * Custom hook managing all booking wizard state and API logic
 * Handles service/barber selection, availability fetching, and booking creation
 */
export function useBookingFlow() {
  // Current step and all step data
  const [currentStep, setCurrentStep] = useState<Step>("service");
  const [categories, setCategories] = useState<CategoryWithServices[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [loadingServices, setLoadingServices] = useState(true);
  const [barbers, setBarbers] = useState<Barber[]>([]);
  const [selectedBarber, setSelectedBarber] = useState<Barber | null>(null);
  const [loadingBarbers, setLoadingBarbers] = useState(false);
  const [selectedDate, setSelectedDate] = useState(() => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  });
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedTime, setSelectedTime] = useState("");
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<
    "pay_online" | "pay_in_shop"
  >("pay_online");
  const [submitting, setSubmitting] = useState(false);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  // Payment state
  const [depositAmount, setDepositAmount] = useState<number | null>(null);
  const [outstandingBalance, setOutstandingBalance] = useState<number | null>(
    null
  );
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [paymentIntentId, setPaymentIntentId] = useState<string | null>(null);
  const [isPaymentProcessing, setIsPaymentProcessing] = useState(false);
  const [createdBookingId, setCreatedBookingId] = useState<number | null>(null);

  // Guest booking state
  const [customerInfo, setCustomerInfo] = useState<CustomerInfo | null>(null);
  const [guestUserId, setGuestUserId] = useState<number | null>(null);

  // Fetch services on mount, fetch slots when date changes
  useEffect(() => {
    fetchServices();
  }, []);

  useEffect(() => {
    if (selectedDate && selectedBarber && selectedService) {
      fetchAvailableSlots();
    }
  }, [selectedDate, selectedBarber, selectedService]);

  /** Fetch all active services grouped by category */
  const fetchServices = async () => {
    try {
      const response = await categoriesApi.getWithServices();
      // Filter out inactive services from each category
      const categoriesWithActiveServices = response.data.map((cat) => ({
        ...cat,
        services: cat.services.filter((s) => s.active !== false),
      }));
      setCategories(categoriesWithActiveServices);
      // Flatten services for backward compatibility
      const allServices = categoriesWithActiveServices.flatMap((cat) =>
        cat.services.map((s) => ({
          ...s,
          categoryId: cat.id,
          categoryName: cat.name,
        }))
      );
      setServices(allServices);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load services" });
    } finally {
      setLoadingServices(false);
    }
  };

  /** Fetch all active barbers */
  const fetchBarbers = async () => {
    setLoadingBarbers(true);
    try {
      const response = await barbersApi.getActive();
      setBarbers(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load barbers" });
    } finally {
      setLoadingBarbers(false);
    }
  };

  /** Fetch available time slots for selected barber, date, and service */
  const fetchAvailableSlots = async () => {
    if (!selectedDate || !selectedBarber || !selectedService) return;

    setLoadingSlots(true);
    try {
      const response = await availabilityApi.getSlots(
        selectedBarber.id,
        selectedDate,
        selectedService.id
      );
      setAvailableSlots(response.data);
      setSelectedTime(""); // Reset selected time when date changes
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load available slots" });
      setAvailableSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  /** Submit booking to backend API */
  const createBooking = async (userId?: number): Promise<number | null> => {
    // If customer info is set, use guest booking flow
    if (customerInfo) {
      return createGuestBooking(customerInfo);
    }

    // Otherwise, use authenticated booking flow (existing logic)
    if (!userId) {
      setStatus({ type: "error", message: "User not authenticated" });
      return null;
    }

    if (!selectedService || !selectedBarber) {
      setStatus({ type: "error", message: "Missing booking details" });
      return null;
    }

    setSubmitting(true);
    try {
      const bookingRequest: BookingRequest = {
        customerId: userId,
        barberId: selectedBarber.id,
        serviceId: selectedService.id,
        bookingDate: selectedDate,
        startTime: selectedTime,
        paymentMethod: paymentMethod,
      };

      const response = await bookingsApi.create(bookingRequest);
      setStatus(null);
      setCreatedBookingId(response.data.id);
      return response.data.id;
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to create booking";
      setStatus({ type: "error", message: String(message) });
      return null;
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Create a booking for a guest customer
   * This creates a guest user account and booking in one call
   */
  const createGuestBooking = async (
    customerInfo: CustomerInfo
  ): Promise<number | null> => {
    if (!selectedService || !selectedBarber) {
      setStatus({ type: "error", message: "Missing booking details" });
      return null;
    }

    setSubmitting(true);
    try {
      const guestBookingRequest = {
        firstName: customerInfo.firstName,
        lastName: customerInfo.lastName,
        email: customerInfo.email,
        phone: customerInfo.phone,
        barberId: selectedBarber.id,
        serviceId: selectedService.id,
        bookingDate: selectedDate,
        startTime: selectedTime,
        paymentMethod: paymentMethod || "pay_online",
      };

      // Call new guest booking endpoint
      const response = await bookingsApi.createGuest(guestBookingRequest);

      setStatus(null);
      setCreatedBookingId(response.data.bookingId);
      setGuestUserId(response.data.customerId);
      setDepositAmount(response.data.depositAmount);
      setOutstandingBalance(response.data.outstandingBalance);

      return response.data.bookingId;
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to create booking";
      setStatus({ type: "error", message: String(message) });
      return null;
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Save a guest account by setting a password
   * Converts guest user to registered user
   */
  const saveGuestAccount = async (
    userId: number,
    password: string
  ): Promise<boolean> => {
    setSubmitting(true);
    try {
      const saveAccountRequest = {
        userId: userId,
        password: password,
      };

      await authApi.saveAccount(saveAccountRequest);

      setStatus({
        type: "success",
        message: "Account saved successfully! Redirecting...",
      });

      return true;
    } catch (error: any) {
      const message = error.response?.data?.message || "Failed to save account";
      setStatus({ type: "error", message: String(message) });
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  /** Initiate payment intent for deposit */
  const initiateDepositPayment = async (bookingId: number) => {
    setIsPaymentProcessing(true);
    try {
      const response = await paymentsApi.createIntent(bookingId);
      setClientSecret(response.data.clientSecret);
      setPaymentIntentId(response.data.paymentIntentId);
      setDepositAmount(response.data.depositAmount);
      setOutstandingBalance(response.data.outstandingBalance);
      setStatus(null);
      return true;
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to initiate payment";
      setStatus({ type: "error", message: String(message) });
      return false;
    } finally {
      setIsPaymentProcessing(false);
    }
  };

  /** Handle successful payment */
  const handlePaymentSuccess = (paymentIntentId: string) => {
    setPaymentIntentId(paymentIntentId);
    setStatus({
      type: "success",
      message: "Payment successful! Your booking is confirmed.",
    });
  };

  // Return all state and handlers organized by step
  return {
    // Step navigation
    currentStep,
    setCurrentStep,

    // Step 1: Service selection
    categories,
    services,
    selectedService,
    setSelectedService,
    loadingServices,

    // Step 2: Barber selection
    barbers,
    selectedBarber,
    setSelectedBarber,
    loadingBarbers,

    // Step 3: Date & time selection
    selectedDate,
    setSelectedDate,
    availableSlots,
    selectedTime,
    setSelectedTime,
    loadingSlots,

    // Step 4: Payment & confirmation
    paymentMethod,
    setPaymentMethod,
    submitting,

    // Payment state
    depositAmount,
    outstandingBalance,
    clientSecret,
    paymentIntentId,
    isPaymentProcessing,
    setIsPaymentProcessing,
    createdBookingId,

    // Guest booking state
    customerInfo,
    setCustomerInfo,
    guestUserId,

    // Status & error handling
    status,
    setStatus,

    // API handlers
    fetchBarbers,
    createBooking,
    createGuestBooking,
    saveGuestAccount,
    initiateDepositPayment,
    handlePaymentSuccess,
  };
}
