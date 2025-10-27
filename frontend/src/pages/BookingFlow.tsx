import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';
import { useBookingFlow } from '../hooks/useBookingFlow';
import { ServiceSelectionStep } from '../components/bookingSteps/ServiceSelectionStep';
import { BarberSelectionStep } from '../components/bookingSteps/BarberSelectionStep';
import { DateTimeSelectionStep } from '../components/bookingSteps/DateTimeSelectionStep';
import { ConfirmationStep } from '../components/bookingSteps/ConfirmationStep';

/**
 * BookingFlow Page
 *
 * Multi-step booking wizard orchestrator that guides customers through:
 * Step 1: Select a service
 * Step 2: Select a barber
 * Step 3: Select date and time
 * Step 4: Confirm and create booking
 */
export default function BookingFlow() {
  const navigate = useNavigate();
  const user = useAppSelector(state => state.auth.user);
  const bookingFlow = useBookingFlow();

  // Calculate minimum selectable date (tomorrow)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  const handleGoBack = () => {
    if (bookingFlow.currentStep === 'barber') {
      bookingFlow.setCurrentStep('service');
    } else if (bookingFlow.currentStep === 'datetime') {
      bookingFlow.setCurrentStep('barber');
    } else if (bookingFlow.currentStep === 'confirmation') {
      bookingFlow.setCurrentStep('datetime');
    }
    bookingFlow.setStatus(null);
  };

  const handleProceedToBarber = () => {
    if (!bookingFlow.selectedService) {
      bookingFlow.setStatus({ type: 'error', message: 'Please select a service' });
      return;
    }
    bookingFlow.setStatus(null);
    bookingFlow.fetchBarbers();
    bookingFlow.setCurrentStep('barber');
  };

  const handleProceedToDateTime = () => {
    if (!bookingFlow.selectedBarber) {
      bookingFlow.setStatus({ type: 'error', message: 'Please select a barber' });
      return;
    }
    bookingFlow.setStatus(null);
    bookingFlow.setCurrentStep('datetime');
  };

  const handleProceedToConfirmation = () => {
    if (!bookingFlow.selectedDate) {
      bookingFlow.setStatus({ type: 'error', message: 'Please select a date' });
      return;
    }
    if (!bookingFlow.selectedTime) {
      bookingFlow.setStatus({ type: 'error', message: 'Please select a time' });
      return;
    }
    bookingFlow.setStatus(null);
    bookingFlow.setCurrentStep('confirmation');
  };

  const handleCreateBooking = async () => {
    if (!user) {
      bookingFlow.setStatus({ type: 'error', message: 'User not authenticated' });
      return;
    }

    const success = await bookingFlow.createBooking(user.id);
    if (success) {
      navigate('/');
    }
  };

  // ============================================
  // STEP 1: SERVICE SELECTION
  // ============================================
  if (bookingFlow.currentStep === 'service') {
    return (
      <ServiceSelectionStep
        services={bookingFlow.services}
        selectedService={bookingFlow.selectedService}
        loading={bookingFlow.loadingServices}
        status={bookingFlow.status}
        onSelect={bookingFlow.setSelectedService}
        onContinue={handleProceedToBarber}
      />
    );
  }

  // ============================================
  // STEP 2: BARBER SELECTION
  // ============================================
  if (bookingFlow.currentStep === 'barber') {
    return (
      <BarberSelectionStep
        barbers={bookingFlow.barbers}
        selectedBarber={bookingFlow.selectedBarber}
        loading={bookingFlow.loadingBarbers}
        status={bookingFlow.status}
        selectedServiceName={bookingFlow.selectedService?.name}
        onSelect={bookingFlow.setSelectedBarber}
        onContinue={handleProceedToDateTime}
        onBack={handleGoBack}
      />
    );
  }

  // ============================================
  // STEP 3: DATE & TIME SELECTION
  // ============================================
  if (bookingFlow.currentStep === 'datetime') {
    return (
      <DateTimeSelectionStep
        selectedDate={bookingFlow.selectedDate}
        availableSlots={bookingFlow.availableSlots}
        selectedTime={bookingFlow.selectedTime}
        loading={bookingFlow.loadingSlots}
        status={bookingFlow.status}
        selectedBarberName={`${bookingFlow.selectedBarber?.user.firstName} ${bookingFlow.selectedBarber?.user.lastName}`}
        minDate={minDate}
        onDateChange={bookingFlow.setSelectedDate}
        onTimeSelect={bookingFlow.setSelectedTime}
        onContinue={handleProceedToConfirmation}
        onBack={handleGoBack}
      />
    );
  }

  // ============================================
  // STEP 4: CONFIRMATION
  // ============================================
  if (bookingFlow.currentStep === 'confirmation') {
    return (
      <ConfirmationStep
        selectedService={bookingFlow.selectedService}
        selectedBarber={bookingFlow.selectedBarber}
        selectedDate={bookingFlow.selectedDate}
        selectedTime={bookingFlow.selectedTime}
        paymentMethod={bookingFlow.paymentMethod}
        status={bookingFlow.status}
        submitting={bookingFlow.submitting}
        onPaymentMethodChange={bookingFlow.setPaymentMethod}
        onConfirm={handleCreateBooking}
        onBack={handleGoBack}
      />
    );
  }

  return null;
}