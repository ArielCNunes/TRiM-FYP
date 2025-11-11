import { useState, useEffect } from "react";
import { availabilityApi, bookingsApi } from "../../api/endpoints";
import type { BookingResponse } from "../../types";
import LoadingSpinner from "../shared/LoadingSpinner";

interface RescheduleBookingModalProps {
  booking: BookingResponse;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * RescheduleBookingModal Component
 *
 * Modal dialog that allows users to reschedule a booking to a new date and time.
 * Fetches available time slots for the selected date and validates the new selection.
 */
export default function RescheduleBookingModal({
  booking,
  isOpen,
  onClose,
  onSuccess,
}: RescheduleBookingModalProps) {
  const [selectedDate, setSelectedDate] = useState<string>("");
  const [selectedTime, setSelectedTime] = useState<string>("");
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Reset form when modal opens/closes or booking changes
  useEffect(() => {
    if (isOpen) {
      setSelectedDate("");
      setSelectedTime("");
      setAvailableSlots([]);
      setError(null);
    }
  }, [isOpen, booking.id]);

  // Fetch available slots when date is selected
  useEffect(() => {
    if (selectedDate && booking) {
      fetchAvailableSlots(selectedDate);
    } else {
      setAvailableSlots([]);
      setSelectedTime("");
    }
  }, [selectedDate, booking]);

  /**
   * Fetch available time slots for the selected date
   */
  const fetchAvailableSlots = async (date: string) => {
    setLoadingSlots(true);
    setError(null);
    try {
      const response = await availabilityApi.getSlots(
        booking.barber.id,
        date,
        booking.service.id
      );
      setAvailableSlots(response.data);

      // If no slots available, show message
      if (response.data.length === 0) {
        setError(
          "No available time slots for this date. Please select another date."
        );
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Failed to load available time slots. Please try again."
      );
      setAvailableSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  /**
   * Handle form submission to update the booking
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedDate || !selectedTime) {
      setError("Please select both a date and time.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await bookingsApi.updateBooking(booking.id, {
        bookingDate: selectedDate,
        startTime: selectedTime,
      });

      // Success! Close modal and notify parent
      onSuccess();
      onClose();
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        "Failed to reschedule booking. Please try again.";
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Get minimum date for date picker (tomorrow)
   */
  const getMinDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  };

  /**
   * Get maximum date for date picker (3 months from now)
   */
  const getMaxDate = () => {
    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + 3);
    return maxDate.toISOString().split("T")[0];
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-xl">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4 rounded-t-lg">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-white">
              Reschedule Appointment
            </h2>
            <button
              onClick={onClose}
              disabled={submitting}
              className="text-white hover:text-gray-200 transition"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>

        {/* Current Booking Details */}
        <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <p className="text-sm font-semibold text-gray-600 mb-2">
            Current Appointment
          </p>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-600">Date:</span>{" "}
              <span className="font-semibold text-gray-900">
                {new Date(booking.bookingDate).toLocaleDateString("en-US", {
                  weekday: "short",
                  month: "short",
                  day: "numeric",
                  year: "numeric",
                })}
              </span>
            </div>
            <div>
              <span className="text-gray-600">Time:</span>{" "}
              <span className="font-semibold text-gray-900">
                {booking.startTime.substring(0, 5)}
              </span>
            </div>
            <div>
              <span className="text-gray-600">Service:</span>{" "}
              <span className="font-semibold text-gray-900">
                {booking.service.name}
              </span>
            </div>
            <div>
              <span className="text-gray-600">Barber:</span>{" "}
              <span className="font-semibold text-gray-900">
                {booking.barber.user.firstName} {booking.barber.user.lastName}
              </span>
            </div>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="px-6 py-6">
          {/* Error Message */}
          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
              <p className="text-sm text-red-700 font-medium">{error}</p>
            </div>
          )}

          {/* Date Selection */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Select New Date
            </label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              min={getMinDate()}
              max={getMaxDate()}
              className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition"
              disabled={submitting}
              required
            />
            <p className="mt-2 text-xs text-gray-500">
              Select a new date for your appointment.
            </p>
          </div>

          {/* Time Slots Selection */}
          {selectedDate && (
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Select New Time
              </label>

              {loadingSlots ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner />
                  <span className="ml-3 text-gray-600">
                    Loading available times...
                  </span>
                </div>
              ) : availableSlots.length > 0 ? (
                <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-64 overflow-y-auto p-2 border border-gray-200 rounded-md">
                  {availableSlots.map((slot) => (
                    <button
                      key={slot}
                      type="button"
                      onClick={() => setSelectedTime(slot)}
                      className={`px-3 py-2 rounded-md text-sm font-medium transition ${
                        selectedTime === slot
                          ? "bg-blue-600 text-white ring-2 ring-blue-500 ring-offset-2"
                          : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                      }`}
                      disabled={submitting}
                    >
                      {slot.substring(0, 5)}
                    </button>
                  ))}
                </div>
              ) : (
                !loadingSlots && (
                  <div className="text-center py-6 bg-gray-50 rounded-md border border-gray-200">
                    <p className="text-gray-600">
                      No available time slots for this date
                    </p>
                    <p className="text-sm text-gray-500 mt-1">
                      Please select a different date
                    </p>
                  </div>
                )
              )}
            </div>
          )}

          {/* Selected New Time Display */}
          {selectedDate && selectedTime && (
            <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-md">
              <p className="text-sm font-semibold text-green-700 mb-1">
                New Appointment Details
              </p>
              <p className="text-green-800">
                <span className="font-semibold">
                  {new Date(selectedDate).toLocaleDateString("en-US", {
                    weekday: "long",
                    month: "long",
                    day: "numeric",
                    year: "numeric",
                  })}
                </span>{" "}
                at{" "}
                <span className="font-semibold">
                  {selectedTime.substring(0, 5)}
                </span>
              </p>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex gap-3 justify-end pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-md hover:bg-gray-50 transition disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !selectedDate || !selectedTime}
              className="px-6 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
            >
              {submitting ? (
                <>
                  <LoadingSpinner />
                  <span className="ml-2">Rescheduling...</span>
                </>
              ) : (
                "Confirm Reschedule"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
