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
    <div className="fixed inset-0 bg-[var(--overlay)] z-50 flex items-center justify-center p-4 backdrop-blur-sm">
      <div className="bg-[var(--bg-surface)] rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-xl border border-[var(--border-subtle)]">
        {/* Header */}
        <div className="bg-gradient-to-r from-[var(--accent)] to-[var(--accent-hover)] px-6 py-4 rounded-t-lg">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-[var(--text-primary)]">
              Reschedule Appointment
            </h2>
            <button
              onClick={onClose}
              disabled={submitting}
              className="text-[var(--text-primary)] hover:text-[var(--text-secondary)] transition"
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
        <div className="px-6 py-4 bg-[var(--bg-elevated)]/50 border-b border-[var(--border-default)]">
          <p className="text-sm font-semibold text-[var(--text-muted)] mb-2">
            Current Appointment
          </p>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-[var(--text-muted)]">Date:</span>{" "}
              <span className="font-semibold text-[var(--text-primary)]">
                {new Date(booking.bookingDate).toLocaleDateString("en-US", {
                  weekday: "short",
                  month: "short",
                  day: "numeric",
                  year: "numeric",
                })}
              </span>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Time:</span>{" "}
              <span className="font-semibold text-[var(--text-primary)]">
                {booking.startTime.substring(0, 5)}
              </span>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Service:</span>{" "}
              <span className="font-semibold text-[var(--text-primary)]">
                {booking.service.name}
              </span>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Barber:</span>{" "}
              <span className="font-semibold text-[var(--text-primary)]">
                {booking.barber.user.firstName} {booking.barber.user.lastName}
              </span>
            </div>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="px-6 py-6">
          {/* Error Message */}
          {error && (
            <div className="mb-4 p-4 bg-[var(--danger-muted)]/20 border border-[var(--danger-border)] rounded-md">
              <p className="text-sm text-[var(--danger-text)] font-medium">{error}</p>
            </div>
          )}

          {/* Date Selection */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-[var(--text-secondary)] mb-2">
              Select New Date
            </label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              min={getMinDate()}
              max={getMaxDate()}
              className="w-full px-4 py-3 bg-[var(--bg-elevated)] border border-[var(--border-default)] text-[var(--text-primary)] rounded-md focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)] transition"
              disabled={submitting}
              required
            />
            <p className="mt-2 text-xs text-[var(--text-subtle)]">
              Select a new date for your appointment.
            </p>
          </div>

          {/* Time Slots Selection */}
          {selectedDate && (
            <div className="mb-6">
              <label className="block text-sm font-semibold text-[var(--text-secondary)] mb-2">
                Select New Time
              </label>

              {loadingSlots ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner />
                  <span className="ml-3 text-[var(--text-muted)]">
                    Loading available times...
                  </span>
                </div>
              ) : availableSlots.length > 0 ? (
                <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-64 overflow-y-auto p-2 border border-[var(--border-default)] rounded-md bg-[var(--bg-elevated)]/30">
                  {availableSlots.map((slot) => (
                    <button
                      key={slot}
                      type="button"
                      onClick={() => setSelectedTime(slot)}
                      className={`px-3 py-2 rounded-md text-sm font-medium transition ${selectedTime === slot
                          ? "bg-[var(--accent)] text-[var(--text-primary)] ring-2 ring-[var(--focus-ring)] ring-offset-2 ring-offset-[var(--bg-surface)]"
                          : "bg-[var(--bg-elevated)] text-[var(--text-secondary)] hover:bg-[var(--bg-muted)] border border-[var(--border-default)]"
                        }`}
                      disabled={submitting}
                    >
                      {slot.substring(0, 5)}
                    </button>
                  ))}
                </div>
              ) : (
                !loadingSlots && (
                  <div className="text-center py-6 bg-[var(--bg-elevated)]/50 rounded-md border border-[var(--border-default)]">
                    <p className="text-[var(--text-muted)]">
                      No available time slots for this date
                    </p>
                    <p className="text-sm text-[var(--text-subtle)] mt-1">
                      Please select a different date
                    </p>
                  </div>
                )
              )}
            </div>
          )}

          {/* Selected New Time Display */}
          {selectedDate && selectedTime && (
            <div className="mb-6 p-4 bg-[var(--success)]/20 border border-[var(--success-border)] rounded-md">
              <p className="text-sm font-semibold text-[var(--success-text)] mb-1">
                New Appointment Details
              </p>
              <p className="text-[var(--success-text)]">
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
          <div className="flex gap-3 justify-end pt-4 border-t border-[var(--border-subtle)]">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-6 py-2 border border-[var(--border-default)] text-[var(--text-secondary)] font-medium rounded-md hover:bg-[var(--bg-elevated)] transition disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !selectedDate || !selectedTime}
              className="px-6 py-2 bg-[var(--accent)] text-[var(--text-primary)] font-medium rounded-md hover:bg-[var(--accent-hover)] transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center shadow-lg shadow-[var(--accent-shadow)]"
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
