import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';
import { useBookingFlow } from '../hooks/useBookingFlow';
import { ServiceSelectionStep } from '../components/bookingSteps/ServiceSelectionStep';
import { BarberSelectionStep } from '../components/bookingSteps/BarberSelectionStep';
import { DateTimeSelectionStep } from '../components/bookingSteps/DateTimeSelectionStep';
import { CustomerInfoStep } from '../components/bookingSteps/CustomerInfoStep';
import { ConfirmationStep } from '../components/bookingSteps/ConfirmationStep';
import { SaveAccountDecisionStep } from '../components/bookingSteps/SaveAccountDecisionStep';
import { PaymentForm } from '../components/bookingSteps/PaymentForm';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || '');

/**
 * BookingFlow Page
 *
 * Multi-step booking wizard orchestrator that guides customers through:
 * Step 1: Select a service
 * Step 2: Select a barber
 * Step 3: Select date and time
 * Step 4: Enter customer information (guest only)
 * Step 5: Confirm and create booking
 * Step 6: Payment
 * Step 7: Save account decision (guest only)
 */
export default function BookingFlow() {
  const navigate = useNavigate();
  const user = useAppSelector(state => state.auth.user);
  const bookingFlow = useBookingFlow();

  // Allow access to booking flow whether authenticated or not
  // User can be null for guests
  const isLoggedIn = !!user;

  // Calculate minimum selectable date (tomorrow)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  const handleGoBack = () => {
    // Navigate backward through steps
    if (bookingFlow.currentStep === 'barber') {
      bookingFlow.setCurrentStep('service');
    } else if (bookingFlow.currentStep === 'datetime') {
      bookingFlow.setCurrentStep('barber');
    } else if (bookingFlow.currentStep === 'customerinfo') {
      bookingFlow.setCurrentStep('datetime');
    } else if (bookingFlow.currentStep === 'confirmation') {
      bookingFlow.setCurrentStep(isLoggedIn ? 'datetime' : 'customerinfo');
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

    // If logged in, skip customer info step and go to confirmation
    if (isLoggedIn) {
      bookingFlow.setCurrentStep('confirmation');
    } else {
      bookingFlow.setCurrentStep('customerinfo');
    }
  };

  const handleCustomerInfoSubmit = (customerInfo: any) => {
    bookingFlow.setCustomerInfo(customerInfo);
    bookingFlow.setCurrentStep('confirmation');
  };

  const handleConfirmBooking = async () => {
    // Determine which user ID to use
    const userId = isLoggedIn ? user?.id : undefined;

    // Create booking (guest or authenticated)
    const bookingId = await bookingFlow.createBooking(userId);
    if (!bookingId) {
      return; // Error already set by createBooking
    }

    // Initiate payment intent
    const success = await bookingFlow.initiateDepositPayment(bookingId);
    if (success) {
      bookingFlow.setCurrentStep('payment');
    }
  };

  const handlePaymentSuccess = (paymentIntentId: string) => {
    bookingFlow.handlePaymentSuccess(paymentIntentId);

    // If guest booking, show save account decision
    if (!isLoggedIn && bookingFlow.guestUserId) {
      bookingFlow.setCurrentStep('saveaccount');
    } else {
      // If logged-in user, redirect to bookings
      setTimeout(() => {
        navigate('/my-bookings');
      }, 2000);
    }
  };

  const handlePaymentError = (error: string) => {
    bookingFlow.setStatus({ type: 'error', message: error });
  };

  const handleSkipSaveAccount = () => {
    // Guest chooses not to save account
    navigate('/');
  };

  // ============================================
  // STEP 1: SERVICE SELECTION
  // ============================================
  if (bookingFlow.currentStep === 'service') {
    return (
      <ServiceSelectionStep
        categories={bookingFlow.categories}
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
  // STEP 4: CUSTOMER INFORMATION (NEW - GUEST ONLY)
  // ============================================
  if (bookingFlow.currentStep === 'customerinfo') {
    return (
      <CustomerInfoStep
        initialData={undefined}
        onSubmit={handleCustomerInfoSubmit}
        status={bookingFlow.status}
        submitting={bookingFlow.submitting}
        onBack={handleGoBack}
      />
    );
  }

  // ============================================
  // STEP 5: CONFIRMATION
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
        onConfirm={handleConfirmBooking}
        onBack={handleGoBack}
      />
    );
  }

  // ============================================
  // STEP 6: PAYMENT
  // ============================================
  if (bookingFlow.currentStep === 'payment' && bookingFlow.clientSecret) {
    return (
      <Elements stripe={stripePromise} options={{ clientSecret: bookingFlow.clientSecret }}>
        <PaymentForm
          clientSecret={bookingFlow.clientSecret}
          bookingId={bookingFlow.createdBookingId || 0}
          depositAmount={bookingFlow.depositAmount || 0}
          onPaymentSuccess={handlePaymentSuccess}
          onPaymentError={handlePaymentError}
          isProcessing={bookingFlow.isPaymentProcessing}
          setIsProcessing={bookingFlow.setIsPaymentProcessing}
        />
      </Elements>
    );
  }

  // ============================================
  // STEP 7: SAVE ACCOUNT DECISION (NEW - GUEST ONLY)
  // ============================================
  if (bookingFlow.currentStep === 'saveaccount' && bookingFlow.guestUserId && bookingFlow.customerInfo) {
    return (
      <SaveAccountDecisionStep
        customerEmail={bookingFlow.customerInfo.email}
        guestUserId={bookingFlow.guestUserId}
        onSave={bookingFlow.saveGuestAccount}
        onSkip={handleSkipSaveAccount}
        status={bookingFlow.status}
        submitting={bookingFlow.submitting}
      />
    );
  }

  return null;
}