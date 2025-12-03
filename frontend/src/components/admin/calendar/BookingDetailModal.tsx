import type { BookingResponse } from "../../../types";

interface BookingDetailModalProps {
    booking: BookingResponse;
    onClose: () => void;
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
            return "bg-green-600/20 text-green-400 border-green-500";
        case "PENDING":
            return "bg-yellow-600/20 text-yellow-400 border-yellow-500";
        case "COMPLETED":
            return "bg-zinc-600/20 text-zinc-400 border-zinc-500";
        case "CANCELLED":
            return "bg-red-600/20 text-red-400 border-red-500";
        case "NO_SHOW":
            return "bg-orange-600/20 text-orange-400 border-orange-500";
        default:
            return "bg-indigo-600/20 text-indigo-400 border-indigo-500";
    }
};

// Get payment status badge style
const getPaymentBadge = (status: string) => {
    switch (status.toUpperCase()) {
        case "PAID":
            return "bg-green-600/20 text-green-400 border-green-500";
        case "DEPOSIT_PAID":
            return "bg-blue-600/20 text-blue-400 border-blue-500";
        case "UNPAID":
            return "bg-red-600/20 text-red-400 border-red-500";
        default:
            return "bg-zinc-600/20 text-zinc-400 border-zinc-500";
    }
};

export default function BookingDetailModal({
    booking,
    onClose,
}: BookingDetailModalProps) {
    return (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
            <div className="bg-zinc-900 rounded-lg shadow-xl border border-zinc-800 w-full max-w-md overflow-hidden">
                {/* Modal Header */}
                <div className="flex items-center justify-between p-4 border-b border-zinc-800">
                    <h3 className="text-lg font-bold text-white">Booking Details</h3>
                    <button
                        onClick={onClose}
                        className="text-zinc-400 hover:text-white transition p-1"
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
                    <div className="bg-zinc-800/50 rounded-lg p-3">
                        <div className="text-sm text-zinc-400">Date & Time</div>
                        <div className="text-white font-medium">
                            {formatDate(booking.bookingDate)}
                        </div>
                        <div className="text-indigo-400">
                            {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
                        </div>
                    </div>

                    {/* Customer Info */}
                    <div className="bg-zinc-800/50 rounded-lg p-3">
                        <div className="text-sm text-zinc-400 mb-1">Customer</div>
                        <div className="text-white font-medium">
                            {booking.customer.firstName} {booking.customer.lastName}
                        </div>
                        <div className="text-sm text-zinc-400">{booking.customer.email}</div>
                        <div className="text-sm text-zinc-400">{booking.customer.phone}</div>
                    </div>

                    {/* Service & Barber */}
                    <div className="grid grid-cols-2 gap-3">
                        <div className="bg-zinc-800/50 rounded-lg p-3">
                            <div className="text-sm text-zinc-400 mb-1">Service</div>
                            <div className="text-white font-medium">{booking.service.name}</div>
                            <div className="text-sm text-zinc-400">
                                {booking.service.durationMinutes} min
                            </div>
                        </div>
                        <div className="bg-zinc-800/50 rounded-lg p-3">
                            <div className="text-sm text-zinc-400 mb-1">Barber</div>
                            <div className="text-white font-medium">
                                {booking.barber.user.firstName} {booking.barber.user.lastName}
                            </div>
                        </div>
                    </div>

                    {/* Payment Info */}
                    <div className="bg-zinc-800/50 rounded-lg p-3">
                        <div className="text-sm text-zinc-400 mb-2">Payment</div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                            <div>
                                <span className="text-zinc-400">Total:</span>
                                <span className="text-white ml-2">
                                    €{booking.service.price.toFixed(2)}
                                </span>
                            </div>
                            {booking.depositAmount !== undefined && (
                                <div>
                                    <span className="text-zinc-400">Deposit:</span>
                                    <span className="text-white ml-2">
                                        €{booking.depositAmount.toFixed(2)}
                                    </span>
                                </div>
                            )}
                            {booking.outstandingBalance !== undefined && (
                                <div>
                                    <span className="text-zinc-400">Outstanding:</span>
                                    <span className="text-amber-400 ml-2">
                                        €{booking.outstandingBalance.toFixed(2)}
                                    </span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Notes */}
                    {booking.notes && (
                        <div className="bg-zinc-800/50 rounded-lg p-3">
                            <div className="text-sm text-zinc-400 mb-1">Notes</div>
                            <div className="text-white text-sm">{booking.notes}</div>
                        </div>
                    )}

                    {/* Booking ID & Created */}
                    <div className="flex justify-between text-xs text-zinc-500 pt-2 border-t border-zinc-800">
                        <span>Booking #{booking.id}</span>
                        <span>
                            Created: {new Date(booking.createdAt).toLocaleDateString()}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
}
