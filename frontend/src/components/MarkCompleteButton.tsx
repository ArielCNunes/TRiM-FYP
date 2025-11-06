import React, { useState } from "react";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";

interface MarkCompleteButtonProps {
  bookingId: number;
  bookingStatus: string;
  onSuccess?: (updatedBooking: BookingResponse) => void;
  onError?: (error: string) => void;
  disabled?: boolean;
}

/**
 * Button component for marking a booking as complete
 * Handles API call, loading states, and error messages
 */
export const MarkCompleteButton: React.FC<MarkCompleteButtonProps> = ({
  bookingId,
  bookingStatus,
  onSuccess,
  onError,
  disabled = false,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastClickTime, setLastClickTime] = useState(0);

  // Button should be disabled if already completed, cancelled, no-show, or loading
  const isDisabled =
    disabled ||
    loading ||
    bookingStatus === "COMPLETED" ||
    bookingStatus === "CANCELLED" ||
    bookingStatus === "NO_SHOW";

  const handleMarkComplete = async () => {
    // Debounce: Prevent multiple rapid clicks
    const now = Date.now();
    if (now - lastClickTime < 1000) {
      return;
    }
    setLastClickTime(now);

    setLoading(true);
    setError(null);

    try {
      const response = await bookingsApi.markComplete(bookingId);
      const updatedBooking = response.data;

      // Verify status actually changed
      if (updatedBooking.status !== "COMPLETED") {
        throw new Error("Booking status did not update correctly");
      }

      if (onSuccess) {
        onSuccess(updatedBooking);
      }

      setError(null);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        "Failed to mark booking as complete";

      setError(errorMessage);

      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const getButtonText = (): string => {
    if (loading) return "Completing...";
    if (bookingStatus === "COMPLETED") return "Completed ✓";
    if (bookingStatus === "CANCELLED") return "Cancelled";
    if (bookingStatus === "NO_SHOW") return "No Show";
    return "Mark Complete";
  };

  const getButtonColor = (): string => {
    if (bookingStatus === "COMPLETED") return "bg-green-600";
    if (bookingStatus === "CANCELLED" || bookingStatus === "NO_SHOW")
      return "bg-red-400";
    if (loading) return "bg-gray-400";
    return "bg-blue-600 hover:bg-blue-700";
  };

  return (
    <div className="flex flex-col gap-2">
      <button
        onClick={handleMarkComplete}
        disabled={isDisabled}
        className={`${getButtonColor()} text-white px-4 py-2 rounded font-semibold transition ${
          isDisabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"
        }`}
        aria-label={`Mark booking ${bookingId} as complete`}
      >
        {getButtonText()}
      </button>

      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-3 py-2 rounded text-sm"
          role="alert"
        >
          {error}
        </div>
      )}
    </div>
  );
};
