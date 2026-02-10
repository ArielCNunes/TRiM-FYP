import { useState } from "react";
import type { BookingResponse } from "../../../types";
import { bookingsApi } from "../../../api/endpoints";

interface BookingDetailModalProps {
    booking: BookingResponse;
    onClose: () => void;
    onStatusChange?: () => void;
    canChangeStatus?: boolean;
}

// Format time for display (HH:mm -> 12-hour format)
const formatTime = (time: string): string => {
    const [hours, minutes] = time.split(":").map(Number);
    const period = hours >= 12 ? "PM" : "AM";
    const displayHour = hours > 12 ? hours - 12 : hours === 0 ? 12 : hours;
    return `${displayHour}:${String(minutes).padStart(2, "0")} ${period}`;
};

// Format date for display
const formatDate = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric",
    });
};

// Get status badge style
const getStatusBadge = (status: string) => {
    switch (status.toUpperCase()) {
        case "CONFIRMED":
            return "bg-[var(--green)]/20 text-[var(--green-text)] border-green-500";
        case "PENDING":
            return "bg-yellow-600/20 text-yellow-400 border-yellow-500";
        case "COMPLETED":
            return "bg-[var(--bg-subtle)]/20 text-[var(--text-muted)] border-zinc-500";
        case "CANCELLED":
            return "bg-[var(--danger)]/20 text-[var(--danger-text)] border-red-500";
        case "NO_SHOW":
            return "bg-[var(--orange)]/20 text-[var(--orange-text)] border-orange-500";
        default:
            return "bg-[var(--accent)]/20 text-[var(--accent-text)] border-[var(--focus-ring)]";
    }
};

// Get payment status badge style
const getPaymentBadge = (status: string) => {
    switch (status.toUpperCase()) {
        case "PAID":
            return "bg-[var(--green)]/20 text-[var(--green-text)] border-green-500";
        case "DEPOSIT_PAID":
            return "bg-[var(--info)]/20 text-[var(--info-text)] border-blue-500";
        case "UNPAID":
            return "bg-[var(--danger)]/20 text-[var(--danger-text)] border-red-500";
        default:
            return "bg-[var(--bg-subtle)]/20 text-[var(--text-muted)] border-zinc-500";
    }
};

export default function BookingDetailModal({
    booking,
    onClose,
    onStatusChange,
    canChangeStatus = false,
}: BookingDetailModalProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleMarkComplete = async () => {
        setIsLoading(true);
        setError(null);
        try {
            await bookingsApi.markComplete(booking.id);
            onStatusChange?.();
            onClose();
        } catch {
            setError("Failed to mark booking as complete");
        } finally {
            setIsLoading(false);
        }
    };

    const handleMarkNoShow = async () => {
        setIsLoading(true);
        setError(null);
        try {
            await bookingsApi.markNoShow(booking.id);
            onStatusChange?.();
            onClose();
        } catch {
            setError("Failed to mark booking as no-show");
        } finally {
            setIsLoading(false);
        }
    };

    const canShowActions = canChangeStatus && booking.status.toUpperCase() === "CONFIRMED";

    return (
        <div className="fixed inset-0 bg-[var(--overlay)] flex items-center justify-center z-50 p-4">
            <div className="bg-[var(--bg-surface)] rounded-lg shadow-xl border border-[var(--border-subtle)] w-full max-w-md overflow-hidden">
                {/* Modal Header */}
                <div className="flex items-center justify-between p-4 border-b border-[var(--border-subtle)]">
                    <h3 className="text-lg font-bold text-[var(--text-primary)]">Booking Details</h3>
                    <button
                        onClick={onClose}
                        className="text-[var(--text-muted)] hover:text-[var(--text-primary)] transition p-1"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
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

                {/* Modal Body */}
                <div className="p-4 space-y-4">
                    {/* Status Badges */}
                    <div className="flex gap-2">
                        <span
                            className={`px-2 py-1 text-xs font-medium rounded border ${getStatusBadge(
                                booking.status
                            )}`}
                        >
                            {booking.status.replace("_", " ")}
                        </span>
                        <span
                            className={`px-2 py-1 text-xs font-medium rounded border ${getPaymentBadge(
                                booking.paymentStatus
                            )}`}
                        >
                            {booking.paymentStatus.replace("_", " ")}
                        </span>
                    </div>

                    {/* Date & Time */}
                    <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                        <div className="text-sm text-[var(--text-muted)]">Date & Time</div>
                        <div className="text-[var(--text-primary)] font-medium">
                            {formatDate(booking.bookingDate)}
                        </div>
                        <div className="text-[var(--accent-text)]">
                            {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
                        </div>
                    </div>

                    {/* Customer Info */}
                    <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                        <div className="text-sm text-[var(--text-muted)] mb-1">Customer</div>
                        <div className="text-[var(--text-primary)] font-medium">
                            {booking.customer.firstName} {booking.customer.lastName}
                        </div>
                        <div className="text-sm text-[var(--text-muted)]">{booking.customer.email}</div>
                        <div className="text-sm text-[var(--text-muted)]">{booking.customer.phone}</div>
                    </div>

                    {/* Service & Barber */}
                    <div className="grid grid-cols-2 gap-3">
                        <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                            <div className="text-sm text-[var(--text-muted)] mb-1">Service</div>
                            <div className="text-[var(--text-primary)] font-medium">{booking.service.name}</div>
                            <div className="text-sm text-[var(--text-muted)]">
                                {booking.service.durationMinutes} min
                            </div>
                        </div>
                        <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                            <div className="text-sm text-[var(--text-muted)] mb-1">Barber</div>
                            <div className="text-[var(--text-primary)] font-medium">
                                {booking.barber.user.firstName} {booking.barber.user.lastName}
                            </div>
                        </div>
                    </div>

                    {/* Payment Info */}
                    <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                        <div className="text-sm text-[var(--text-muted)] mb-2">Payment</div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                            <div>
                                <span className="text-[var(--text-muted)]">Total:</span>
                                <span className="text-[var(--text-primary)] ml-2">
                                    €{booking.service.price.toFixed(2)}
                                </span>
                            </div>
                            {booking.depositAmount !== undefined && (
                                <div>
                                    <span className="text-[var(--text-muted)]">Deposit:</span>
                                    <span className="text-[var(--text-primary)] ml-2">
                                        €{booking.depositAmount.toFixed(2)}
                                    </span>
                                </div>
                            )}
                            {booking.outstandingBalance !== undefined && (
                                <div>
                                    <span className="text-[var(--text-muted)]">Outstanding:</span>
                                    <span className="text-[var(--warning-text)] ml-2">
                                        €{booking.outstandingBalance.toFixed(2)}
                                    </span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Notes */}
                    {booking.notes && (
                        <div className="bg-[var(--bg-elevated)]/50 rounded-lg p-3">
                            <div className="text-sm text-[var(--text-muted)] mb-1">Notes</div>
                            <div className="text-[var(--text-primary)] text-sm">{booking.notes}</div>
                        </div>
                    )}

                    {/* Booking ID & Created */}
                    <div className="flex justify-between text-xs text-[var(--text-subtle)] pt-2 border-t border-[var(--border-subtle)]">
                        <span>Booking #{booking.id}</span>
                        <span>
                            Created: {new Date(booking.createdAt).toLocaleDateString()}
                        </span>
                    </div>

                    {/* Error Message */}
                    {error && (
                        <div className="mt-3 p-2 bg-[var(--danger)]/20 border border-red-500 rounded text-[var(--danger-text)] text-sm text-center">
                            {error}
                        </div>
                    )}

                    {/* Action Buttons */}
                    {canShowActions && (
                        <div className="mt-4 pt-4 border-t border-[var(--border-subtle)] flex gap-3">
                            <button
                                onClick={handleMarkComplete}
                                disabled={isLoading}
                                className="flex-1 py-2 px-4 bg-[var(--green)] hover:bg-green-700 disabled:bg-green-800 disabled:cursor-not-allowed text-[var(--text-primary)] rounded-lg font-medium transition"
                            >
                                {isLoading ? "Updating..." : "Mark Complete"}
                            </button>
                            <button
                                onClick={handleMarkNoShow}
                                disabled={isLoading}
                                className="flex-1 py-2 px-4 bg-[var(--orange)] hover:bg-orange-700 disabled:bg-orange-800 disabled:cursor-not-allowed text-[var(--text-primary)] rounded-lg font-medium transition"
                            >
                                {isLoading ? "Updating..." : "Mark No-Show"}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
