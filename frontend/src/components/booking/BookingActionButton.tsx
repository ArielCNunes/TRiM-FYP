import React, { useState } from "react";
import { bookingsApi } from "../../api/endpoints";
import type { BookingResponse } from "../../types";
import { getBookingActionButtonStyles } from "../../utils/statusUtils";

interface BookingActionButtonProps {
  bookingId: number;
  bookingStatus: string;
  actionType: "complete" | "no-show";
  onSuccess?: (updatedBooking: BookingResponse) => void;
  onError?: (error: string) => void;
  disabled?: boolean;
}

/**
 * Generic button component for booking actions (mark complete, mark no-show)
 * Handles API calls, loading states, and error messages
 */
export const BookingActionButton: React.FC<BookingActionButtonProps> = ({
  bookingId,
  bookingStatus,
  actionType,
  onSuccess,
  onError,
  disabled = false,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastClickTime, setLastClickTime] = useState(0);

  // Button should be disabled if already in a terminal state or loading
  const isDisabled =
    disabled ||
    loading ||
    bookingStatus === "COMPLETED" ||
    bookingStatus === "CANCELLED" ||
    bookingStatus === "NO_SHOW";

  const handleAction = async () => {
    // Debounce: Prevent multiple rapid clicks
    const now = Date.now();
    if (now - lastClickTime < 1000) {
      return;
    }
    setLastClickTime(now);

    setLoading(true);
    setError(null);

    try {
      const response =
        actionType === "complete"
          ? await bookingsApi.markComplete(bookingId)
          : await bookingsApi.markNoShow(bookingId);

      const updatedBooking = response.data;
      const expectedStatus =
        actionType === "complete" ? "COMPLETED" : "NO_SHOW";

      // Verify status actually changed
      if (updatedBooking.status !== expectedStatus) {
        throw new Error("Booking status did not update correctly");
      }

      if (onSuccess) {
        onSuccess(updatedBooking);
      }

      setError(null);
    } catch (err: any) {
      const actionText = actionType === "complete" ? "complete" : "no-show";
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        `Failed to mark booking as ${actionText}`;

      setError(errorMessage);

      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const getButtonText = (): string => {
    if (loading) {
      return actionType === "complete" ? "Completing..." : "Processing...";
    }

    if (bookingStatus === "COMPLETED") return "Completed ✓";
    if (bookingStatus === "NO_SHOW") return "No-Show";
    if (bookingStatus === "CANCELLED") return "Cancelled";

    return actionType === "complete" ? "Mark Complete" : "Mark No-Show";
  };

  return (
    <div className="flex flex-col gap-2">
      <button
        onClick={handleAction}
        disabled={isDisabled}
        className={`${getBookingActionButtonStyles(
          bookingStatus,
          loading,
          actionType
        )} text-white px-4 py-2 rounded font-semibold transition ${
          isDisabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"
        }`}
        aria-label={`Mark booking ${bookingId} as ${
          actionType === "complete" ? "complete" : "no-show"
        }`}
      >
        {getButtonText()}
      </button>

      {error && (
        <div
          className="bg-red-900/20 border border-red-800 text-red-300 px-3 py-2 rounded text-sm"
          role="alert"
        >
          {error}
        </div>
      )}
    </div>
  );
};

export default BookingActionButton;
