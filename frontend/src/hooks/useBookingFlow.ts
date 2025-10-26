import { useState, useEffect } from 'react';
import { servicesApi, barbersApi, availabilityApi, bookingsApi } from '../api/endpoints';
import type { Service, Barber, BookingRequest } from '../types';

/** Booking wizard steps: service → barber → datetime → confirmation */
type Step = 'service' | 'barber' | 'datetime' | 'confirmation';

/**
 * Custom hook managing all booking wizard state and API logic
 * Handles service/barber selection, availability fetching, and booking creation
 */
export function useBookingFlow() {
  // Current step and all step data
  const [currentStep, setCurrentStep] = useState<Step>('service');
  const [services, setServices] = useState<Service[]>([]);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [loadingServices, setLoadingServices] = useState(true);
  const [barbers, setBarbers] = useState<Barber[]>([]);
  const [selectedBarber, setSelectedBarber] = useState<Barber | null>(null);
  const [loadingBarbers, setLoadingBarbers] = useState(false);
  const [selectedDate, setSelectedDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedTime, setSelectedTime] = useState('');
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<'pay_online' | 'pay_in_shop'>('pay_online');
  const [submitting, setSubmitting] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  // Fetch services on mount, fetch slots when date changes
  useEffect(() => {
    fetchServices();
  }, []);

  useEffect(() => {
    if (selectedDate && selectedBarber && selectedService) {
      fetchAvailableSlots();
    }
  }, [selectedDate]);

  /** Fetch all active services */
  const fetchServices = async () => {
    try {
      const response = await servicesApi.getActive();
      setServices(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: 'error', message: 'Failed to load services' });
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
      setStatus({ type: 'error', message: 'Failed to load barbers' });
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
      setSelectedTime(''); // Reset selected time when date changes
      setStatus(null);
    } catch (error) {
      setStatus({ type: 'error', message: 'Failed to load available slots' });
      setAvailableSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  /** Submit booking to backend API */
  const createBooking = async (userId: number) => {
    if (!selectedService || !selectedBarber) {
      setStatus({ type: 'error', message: 'Missing booking details' });
      return false;
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

      await bookingsApi.create(bookingRequest);
      setStatus(null);
      return true;
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create booking';
      setStatus({ type: 'error', message: String(message) });
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Return all state and handlers organized by step
  return {
    // Step navigation
    currentStep,
    setCurrentStep,

    // Step 1: Service selection
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

    // Status & error handling
    status,
    setStatus,

    // API handlers
    fetchBarbers,
    createBooking,
  };
}
