import type { Service, Barber } from "../../types";
import { StatusBanner } from "../BookingComponents";

/**
 * ConfirmationStep Component
 * Step 4 of booking wizard: review booking details, select payment method, confirm
 * Displays service, barber, date/time, price; allows payment method selection before submission
 */
export function ConfirmationStep({
  selectedService,
  selectedBarber,
  selectedDate,
  selectedTime,
  status,
  submitting,
  onConfirm,
  onBack,
}: {
  selectedService: Service | null;
  selectedBarber: Barber | null;
  selectedDate: string;
  selectedTime: string;
  paymentMethod: "pay_online" | "pay_in_shop";
  status: { type: "success" | "error"; message: string } | null;
  submitting: boolean;
  onPaymentMethodChange: (method: "pay_online" | "pay_in_shop") => void;
  onConfirm: () => void;
  onBack: () => void;
}) {
  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8">Confirm Your Booking</h1>
      <StatusBanner status={status} />

      {/* Booking summary: service, barber, date/time, price */}
      <div className="border border-gray-200 rounded-lg p-6 mb-6 space-y-4">
        <div className="pb-4 border-b">
          <p className="text-gray-600">Service</p>
          <p className="text-2xl font-bold">{selectedService?.name}</p>
          <p className="text-gray-500">
            Duration: {selectedService?.durationMinutes} minutes
          </p>
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
          <p className="text-2xl font-bold text-blue-600">
            €{selectedService?.price.toFixed(2)}
          </p>
        </div>
      </div>

      {/* Payment Information */}
      <div className="border border-gray-200 bg-gray-50 rounded-lg p-4 mb-6">
        <p className="text-sm text-gray-600 text-center">
          You'll be asked to pay a deposit before your booking is confirmed.
        </p>
      </div>

      {/* Back button and confirm button with loading state */}
      <div className="flex gap-4">
        <button
          onClick={onBack}
          className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Back
        </button>
        <button
          onClick={onConfirm}
          disabled={submitting}
          className="flex-1 bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
        >
          {submitting ? "Creating Booking..." : "Confirm Booking"}
        </button>
      </div>
    </div>
  );
}
