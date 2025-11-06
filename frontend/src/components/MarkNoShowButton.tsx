import React, { useState } from "react";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";

interface MarkNoShowButtonProps {
  bookingId: number;
  bookingStatus: string;
  onSuccess?: (updatedBooking: BookingResponse) => void;
  onError?: (error: string) => void;
  disabled?: boolean;
}

/**
 * Button component for marking a booking as no-show
 * Handles API call, loading states, and error messages
 */
export const MarkNoShowButton: React.FC<MarkNoShowButtonProps> = ({
  bookingId,
  bookingStatus,
  onSuccess,
  onError,
  disabled = false,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastClickTime, setLastClickTime] = useState(0);

  // Button should be disabled if already no-show, completed, cancelled, or loading
  const isDisabled =
    disabled ||
    loading ||
    bookingStatus === "NO_SHOW" ||
    bookingStatus === "COMPLETED" ||
    bookingStatus === "CANCELLED";

  const handleMarkNoShow = async () => {
    // Debounce: Prevent multiple rapid clicks
    const now = Date.now();
    if (now - lastClickTime < 1000) {
      return;
    }
    setLastClickTime(now);

    setLoading(true);
    setError(null);

    try {
      const response = await bookingsApi.markNoShow(bookingId);
      const updatedBooking = response.data;

      // Verify status actually changed
      if (updatedBooking.status !== "NO_SHOW") {
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
        "Failed to mark booking as no-show";

      setError(errorMessage);

      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const getButtonText = (): string => {
    if (loading) return "Processing...";
    if (bookingStatus === "NO_SHOW") return "No-Show";
    if (bookingStatus === "COMPLETED") return "Completed";
    if (bookingStatus === "CANCELLED") return "Cancelled";
    return "Mark No-Show";
  };

  const getButtonColor = (): string => {
    if (bookingStatus === "NO_SHOW") return "bg-gray-600";
    if (bookingStatus === "COMPLETED") return "bg-green-600";
    if (bookingStatus === "CANCELLED") return "bg-red-400";
    if (loading) return "bg-gray-400";
    return "bg-orange-600 hover:bg-orange-700";
  };

  return (
    <div className="flex flex-col gap-2">
      <button
        onClick={handleMarkNoShow}
        disabled={isDisabled}
        className={`${getButtonColor()} text-white px-4 py-2 rounded font-semibold transition ${
          isDisabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"
        }`}
        aria-label={`Mark booking ${bookingId} as no-show`}
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
