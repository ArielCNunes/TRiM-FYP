import { useState, useEffect } from 'react';
import { useAppSelector } from '../store/hooks';
import { servicesApi, barbersApi, availabilityApi, bookingsApi } from '../api/endpoints';
import type { Service, Barber, BookingRequest } from '../types';
import { useNavigate } from 'react-router-dom';

// Type definition for the booking wizard steps
type Step = 'service' | 'barber' | 'datetime' | 'confirmation';

/**
 * BookingFlow Component
 * 
 * Multi-step booking wizard for customers to create appointments:
 * Step 1: Select a service
 * Step 2: Select a barber
 * Step 3: Select date and time
 * Step 4: Confirm and create booking
 */
export default function BookingFlow() {
  const navigate = useNavigate();
  const user = useAppSelector(state => state.auth.user);

  // Step management
  const [currentStep, setCurrentStep] = useState<Step>('service');

  // Step 1: Service selection
  const [services, setServices] = useState<Service[]>([]);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [loadingServices, setLoadingServices] = useState(true);

  // Step 2: Barber selection
  const [barbers, setBarbers] = useState<Barber[]>([]);
  const [selectedBarber, setSelectedBarber] = useState<Barber | null>(null);
  const [loadingBarbers, setLoadingBarbers] = useState(false);

  // Step 3: Date and time selection
  const [selectedDate, setSelectedDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedTime, setSelectedTime] = useState('');
  const [loadingSlots, setLoadingSlots] = useState(false);

  // Step 4: Payment method
  const [paymentMethod, setPaymentMethod] = useState<'pay_online' | 'pay_in_shop'>('pay_online');
  const [submitting, setSubmitting] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  /** Fetch services on component mount */
  useEffect(() => {
    fetchServices();
  }, []);

  /** Fetch availability when date changes */
  useEffect(() => {
    if (selectedDate && selectedBarber && selectedService) {
      fetchAvailableSlots();
    }
  }, [selectedDate]);

  /** Fetch all active services from the API */
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

  /** Fetch all active barbers from the API */
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
    // Convert date from input (YYYY-MM-DD) format
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

  /** Handle service selection in step 1 */
  const handleServiceSelect = (service: Service) => {
    setSelectedService(service);
    setStatus(null);
  };

  /** Proceed to barber selection step */
  const handleProceedToBarber = () => {
    if (!selectedService) {
      setStatus({ type: 'error', message: 'Please select a service' });
      return;
    }
    setStatus(null);
    fetchBarbers();
    setCurrentStep('barber');
  };

  /** Handle barber selection in step 2 */
  const handleBarberSelect = (barber: Barber) => {
    setSelectedBarber(barber);
    setStatus(null);
  };

  /** Proceed to date/time selection step */
  const handleProceedToDateTime = () => {
    if (!selectedBarber) {
      setStatus({ type: 'error', message: 'Please select a barber' });
      return;
    }
    setStatus(null);
    setCurrentStep('datetime');
  };

  /** Proceed to confirmation step */
  const handleProceedToConfirmation = () => {
    if (!selectedDate) {
      setStatus({ type: 'error', message: 'Please select a date' });
      return;
    }
    if (!selectedTime) {
      setStatus({ type: 'error', message: 'Please select a time' });
      return;
    }
    setStatus(null);
    setCurrentStep('confirmation');
  };

  /** Create and submit the booking to the API */
  const handleCreateBooking = async () => {
    if (!user || !selectedService || !selectedBarber) {
      setStatus({ type: 'error', message: 'Missing booking details' });
      return;
    }

    setSubmitting(true);
    try {
      const bookingRequest: BookingRequest = {
        customerId: user.id || 0,
        barberId: selectedBarber.id,
        serviceId: selectedService.id,
        bookingDate: selectedDate,
        startTime: selectedTime,
        paymentMethod: paymentMethod,
      };

      await bookingsApi.create(bookingRequest);
      setStatus(null);
      navigate('/');
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create booking';
      setStatus({ type: 'error', message: String(message) });
    } finally {
      setSubmitting(false);
    }
  };

  /** Navigate back to the previous step */
  const goBack = () => {
    if (currentStep === 'barber') {
      setCurrentStep('service');
    } else if (currentStep === 'datetime') {
      setCurrentStep('barber');
    } else if (currentStep === 'confirmation') {
      setCurrentStep('datetime');
    }
    setStatus(null);
  };

  // Calculate minimum selectable date (tomorrow)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  const statusBanner =
    status && (
      <div
        className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
          status.type === 'success'
            ? 'border-green-200 bg-green-50 text-green-700'
            : 'border-red-200 bg-red-50 text-red-700'
        }`}
      >
        {status.message}
      </div>
    );

  // ============================================
  // STEP 1: SERVICE SELECTION
  // ============================================
  if (currentStep === 'service') {
    if (loadingServices) {
      return <div className="p-8 text-center">Loading services...</div>;
    }

    return (
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-3xl font-bold mb-8">Select a Service</h1>
        {statusBanner}

        {services.length === 0 ? (
          <p className="text-center text-gray-500">No services available</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {services.map((service) => (
              <div
                key={service.id}
                onClick={() => handleServiceSelect(service)}
                className={`border rounded-lg p-6 cursor-pointer transition ${
                  selectedService?.id === service.id
                    ? 'border-blue-600 bg-blue-50'
                    : 'border-gray-200 hover:shadow-lg'
                }`}
              >
                <h3 className="text-xl font-bold mb-2">{service.name}</h3>
                <p className="text-gray-600 mb-4">{service.description}</p>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-500">{service.durationMinutes} min</span>
                  <span className="text-lg font-bold text-blue-600">€{service.price.toFixed(2)}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        {selectedService && (
          <div className="mt-8 p-4 bg-blue-50 rounded-lg">
            <p className="text-lg mb-4">
              Selected: <strong>{selectedService.name}</strong> - €{selectedService.price.toFixed(2)}
            </p>
            <button
              onClick={handleProceedToBarber}
              className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
            >
              Continue to Barber Selection
            </button>
          </div>
        )}
      </div>
    );
  }

  // ============================================
  // STEP 2: BARBER SELECTION
  // ============================================
  if (currentStep === 'barber') {
    if (loadingBarbers) {
      return <div className="p-8 text-center">Loading barbers...</div>;
    }

    return (
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-3xl font-bold mb-2">Select a Barber</h1>
        <p className="text-gray-600 mb-8">Service: <strong>{selectedService?.name}</strong></p>
        {statusBanner}

        {barbers.length === 0 ? (
          <p className="text-center text-gray-500">No barbers available</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {barbers.map((barber) => (
              <div
                key={barber.id}
                onClick={() => handleBarberSelect(barber)}
                className={`border rounded-lg p-6 cursor-pointer transition ${
                  selectedBarber?.id === barber.id
                    ? 'border-blue-600 bg-blue-50'
                    : 'border-gray-200 hover:shadow-lg'
                }`}
              >
                {barber.profileImageUrl && (
                  <img
                    src={barber.profileImageUrl}
                    alt={barber.user.firstName}
                    className="w-full h-40 object-cover rounded-lg mb-4"
                  />
                )}
                <h3 className="text-xl font-bold mb-2">
                  {barber.user.firstName} {barber.user.lastName}
                </h3>
                {barber.bio && <p className="text-gray-600 mb-4">{barber.bio}</p>}
                <p className="text-sm text-gray-500">{barber.user.email}</p>
              </div>
            ))}
          </div>
        )}

        <div className="mt-8 flex gap-4">
          <button
            onClick={goBack}
            className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Back
          </button>
          {selectedBarber && (
            <button
              onClick={handleProceedToDateTime}
              className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
            >
              Continue to Date & Time
            </button>
          )}
        </div>
      </div>
    );
  }

  // ============================================
  // STEP 3: DATE & TIME SELECTION
  // ============================================
  if (currentStep === 'datetime') {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-3xl font-bold mb-2">Select Date & Time</h1>
        <p className="text-gray-600 mb-8">
          Barber: <strong>{selectedBarber?.user.firstName} {selectedBarber?.user.lastName}</strong>
        </p>
        {statusBanner}

        <div className="space-y-6">
          {/* Date Picker */}
          <div>
            <label className="block text-lg font-semibold mb-2">Date</label>
            <input
              type="date"
              min={minDate}
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="w-full border border-gray-300 rounded-lg p-3"
            />
          </div>

          {/* Time Slots */}
          {selectedDate && (
            <div>
              <label className="block text-lg font-semibold mb-2">Available Times</label>
              {loadingSlots ? (
                <p className="text-gray-500">Loading available slots...</p>
              ) : availableSlots.length === 0 ? (
                <p className="text-gray-500">No available slots for this date</p>
              ) : (
                <div className="grid grid-cols-4 gap-2">
                  {availableSlots.map((slot) => (
                    <button
                      key={slot}
                      onClick={() => setSelectedTime(slot)}
                      className={`border rounded-lg p-3 text-center font-semibold transition ${
                        selectedTime === slot
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'border-gray-300 hover:border-blue-600'
                      }`}
                    >
                      {slot}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        <div className="mt-8 flex gap-4">
          <button
            onClick={goBack}
            className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Back
          </button>
          {selectedTime && (
            <button
              onClick={handleProceedToConfirmation}
              className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
            >
              Continue to Confirmation
            </button>
          )}
        </div>
      </div>
    );
  }

  // ============================================
  // STEP 4: CONFIRMATION
  // ============================================
  if (currentStep === 'confirmation') {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-3xl font-bold mb-8">Confirm Your Booking</h1>
        {statusBanner}

        <div className="border border-gray-200 rounded-lg p-6 mb-6 space-y-4">
          <div className="pb-4 border-b">
            <p className="text-gray-600">Service</p>
            <p className="text-2xl font-bold">{selectedService?.name}</p>
            <p className="text-gray-500">Duration: {selectedService?.durationMinutes} minutes</p>
          </div>

          <div className="pb-4 border-b">
            <p className="text-gray-600">Barber</p>
            <p className="text-2xl font-bold">
              {selectedBarber?.user.firstName} {selectedBarber?.user.lastName}
            </p>
          </div>

          <div className="pb-4 border-b">
            <p className="text-gray-600">Date & Time</p>
            <p className="text-2xl font-bold">
              {new Date(selectedDate).toLocaleDateString()} at {selectedTime}
            </p>
          </div>

          <div>
            <p className="text-gray-600">Price</p>
            <p className="text-2xl font-bold text-blue-600">€{selectedService?.price.toFixed(2)}</p>
          </div>
        </div>

        {/* Payment Method */}
        <div className="mb-6">
          <label className="block text-lg font-semibold mb-4">Payment Method</label>
          <div className="space-y-3">
            <label className="flex items-center border rounded-lg p-4 cursor-pointer hover:bg-gray-50">
              <input
                type="radio"
                name="payment"
                value="pay_online"
                checked={paymentMethod === 'pay_online'}
                onChange={(e) => setPaymentMethod(e.target.value as 'pay_online' | 'pay_in_shop')}
                className="mr-3"
              />
              <div>
                <p className="font-semibold">Pay Online</p>
                <p className="text-sm text-gray-500">Pay now with card</p>
              </div>
            </label>

            <label className="flex items-center border rounded-lg p-4 cursor-pointer hover:bg-gray-50">
              <input
                type="radio"
                name="payment"
                value="pay_in_shop"
                checked={paymentMethod === 'pay_in_shop'}
                onChange={(e) => setPaymentMethod(e.target.value as 'pay_online' | 'pay_in_shop')}
                className="mr-3"
              />
              <div>
                <p className="font-semibold">Pay in Shop</p>
                <p className="text-sm text-gray-500">Pay when you arrive</p>
              </div>
            </label>
          </div>
        </div>

        <div className="flex gap-4">
          <button
            onClick={goBack}
            className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Back
          </button>
          <button
            onClick={handleCreateBooking}
            disabled={submitting}
            className="flex-1 bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {submitting ? 'Creating Booking...' : 'Confirm Booking'}
          </button>
        </div>
      </div>
    );
  }

  return null;
}