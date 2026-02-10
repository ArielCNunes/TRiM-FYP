import { useMemo } from "react";
import type { BookingResponse, Barber, BarberBreak } from "../../../types";

interface CalendarGridProps {
    weekDates: Date[];
    bookings: BookingResponse[];
    barbers: Barber[];
    barberBreaks: Map<number, BarberBreak[]>;
    onBookingClick: (booking: BookingResponse) => void;
}

// Shop hours configuration
const SHOP_START_HOUR = 6; // 6 AM
const SHOP_END_HOUR = 22; // 10 PM
const SLOT_HEIGHT = 90; // pixels per hour

// Helper to convert time string (HH:mm) to minutes from midnight
const timeToMinutes = (time: string): number => {
    const [hours, minutes] = time.split(":").map(Number);
    return hours * 60 + minutes;
};

// Helper to format time for display
const formatTime = (hour: number): string => {
    const period = hour >= 12 ? "PM" : "AM";
    const displayHour = hour > 12 ? hour - 12 : hour === 0 ? 12 : hour;
    return `${displayHour} ${period}`;
};

// Generate time slots for the day
const generateTimeSlots = (): number[] => {
    const slots: number[] = [];
    for (let hour = SHOP_START_HOUR; hour < SHOP_END_HOUR; hour++) {
        slots.push(hour);
    }
    return slots;
};

// Format date as YYYY-MM-DD
const formatDateKey = (date: Date): string => {
    return date.toISOString().split("T")[0];
};

// Check if a date is today
const isToday = (date: Date): boolean => {
    const today = new Date();
    return (
        date.getDate() === today.getDate() &&
        date.getMonth() === today.getMonth() &&
        date.getFullYear() === today.getFullYear()
    );
};

// Get day name abbreviation
const getDayName = (date: Date): string => {
    return date.toLocaleDateString("en-US", { weekday: "short" });
};

export default function CalendarGrid({
    weekDates,
    bookings,
    barbers,
    barberBreaks,
    onBookingClick,
}: CalendarGridProps) {
    const timeSlots = useMemo(() => generateTimeSlots(), []);

    // Group bookings by date
    const bookingsByDate = useMemo(() => {
        const map = new Map<string, BookingResponse[]>();
        bookings.forEach((booking) => {
            const dateKey = booking.bookingDate;
            if (!map.has(dateKey)) {
                map.set(dateKey, []);
            }
            map.get(dateKey)!.push(booking);
        });
        return map;
    }, [bookings]);

    // Calculate position and height for a booking block
    const getBlockStyle = (startTime: string, endTime: string) => {
        const startMinutes = timeToMinutes(startTime);
        const endMinutes = timeToMinutes(endTime);
        const shopStartMinutes = SHOP_START_HOUR * 60;

        const top = ((startMinutes - shopStartMinutes) / 60) * SLOT_HEIGHT;
        const height = ((endMinutes - startMinutes) / 60) * SLOT_HEIGHT;

        return { top, height };
    };

    // Get status-based styling for booking blocks
    const getStatusStyle = (status: string): { className: string; bgColor: string; hoverBgColor: string } => {
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return {
                    className: "border-l-4 border-l-green-500",
                    bgColor: "rgba(22, 163, 74, 0.7)", // green-600 with 70% opacity
                    hoverBgColor: "rgba(22, 163, 74, 0.85)",
                };
            case "PENDING":
                return {
                    className: "border-l-4 border-l-yellow-500",
                    bgColor: "rgba(202, 138, 4, 0.7)", // yellow-600 with 70% opacity
                    hoverBgColor: "rgba(202, 138, 4, 0.85)",
                };
            case "COMPLETED":
                return {
                    className: "border-l-4 border-l-zinc-500",
                    bgColor: "rgba(113, 113, 122, 0.7)", // zinc-500 with 70% opacity
                    hoverBgColor: "rgba(113, 113, 122, 0.85)",
                };
            case "NO_SHOW":
                return {
                    className: "border-l-4 border-l-orange-500",
                    bgColor: "rgba(234, 88, 12, 0.7)", // orange-600 with 70% opacity
                    hoverBgColor: "rgba(234, 88, 12, 0.85)",
                };
            default:
                return {
                    className: "border-l-4 border-l-indigo-500",
                    bgColor: "rgba(79, 70, 229, 0.7)", // indigo-600 with 70% opacity
                    hoverBgColor: "rgba(79, 70, 229, 0.85)",
                };
        }
    };

    return (
        <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] overflow-hidden">
            {/* Header row with day names */}
            <div className="flex border-b border-[var(--border-subtle)]">
                {/* Time column header */}
                <div className="w-16 flex-shrink-0 bg-[var(--bg-surface)] p-2 border-r border-[var(--border-subtle)]" />

                {/* Day headers */}
                {weekDates.map((date) => (
                    <div
                        key={formatDateKey(date)}
                        className={`flex-1 p-3 text-center border-r border-[var(--border-subtle)] last:border-r-0 ${isToday(date) ? "bg-[var(--accent-muted)]/20" : ""
                            }`}
                    >
                        <div
                            className={`text-xs font-medium ${isToday(date) ? "text-[var(--accent-text)]" : "text-[var(--text-subtle)]"
                                }`}
                        >
                            {getDayName(date)}
                        </div>
                        <div
                            className={`text-lg font-bold ${isToday(date) ? "text-[var(--accent-text)]" : "text-[var(--text-primary)]"
                                }`}
                        >
                            {date.getDate()}
                        </div>
                    </div>
                ))}
            </div>

            {/* Scrollable body */}
            <div className="overflow-y-auto max-h-[calc(100vh-200px)]">
                <div className="flex relative">
                    {/* Time labels column */}
                    <div className="w-16 flex-shrink-0 bg-[var(--bg-surface)] border-r border-[var(--border-subtle)]">
                        {timeSlots.map((hour) => (
                            <div
                                key={hour}
                                className="border-b border-[var(--border-subtle)] text-xs text-[var(--text-subtle)] pr-2 text-right"
                                style={{ height: SLOT_HEIGHT }}
                            >
                                <span className="relative -top-2">{formatTime(hour)}</span>
                            </div>
                        ))}
                    </div>

                    {/* Day columns */}
                    {weekDates.map((date) => {
                        const dateKey = formatDateKey(date);
                        const dayBookings = bookingsByDate.get(dateKey) || [];

                        return (
                            <div
                                key={dateKey}
                                className={`flex-1 relative border-r border-[var(--border-subtle)] last:border-r-0 ${isToday(date) ? "bg-[var(--accent-muted)]/10" : ""
                                    }`}
                            >
                                {/* Hour grid lines */}
                                {timeSlots.map((hour) => (
                                    <div
                                        key={hour}
                                        className="border-b border-[var(--border-subtle)]"
                                        style={{ height: SLOT_HEIGHT }}
                                    />
                                ))}

                                {/* Break blocks for each barber */}
                                {barbers.map((barber) => {
                                    const breaks = barberBreaks.get(barber.id) || [];

                                    return breaks.map((breakItem, breakIndex) => {
                                        // Normalize time format
                                        const startTime = breakItem.startTime.substring(0, 5);
                                        const endTime = breakItem.endTime.substring(0, 5);

                                        // Clamp to shop hours
                                        const clampedStartMinutes = Math.max(
                                            timeToMinutes(startTime),
                                            SHOP_START_HOUR * 60
                                        );
                                        const clampedEndMinutes = Math.min(
                                            timeToMinutes(endTime),
                                            SHOP_END_HOUR * 60
                                        );

                                        // Skip if break is entirely outside shop hours
                                        if (clampedStartMinutes >= clampedEndMinutes) {
                                            return null;
                                        }

                                        const shopStartMinutes = SHOP_START_HOUR * 60;
                                        const top = ((clampedStartMinutes - shopStartMinutes) / 60) * SLOT_HEIGHT;
                                        const height = ((clampedEndMinutes - clampedStartMinutes) / 60) * SLOT_HEIGHT;

                                        return (
                                            <div
                                                key={`break-${barber.id}-${breakIndex}`}
                                                className="absolute left-1 right-1 bg-[var(--bg-muted)]/40 border border-dashed border-zinc-500 rounded pointer-events-none overflow-hidden flex items-center"
                                                style={{
                                                    top: top + 2,
                                                    height: Math.max(height - 4, 30),
                                                    zIndex: 5,
                                                }}
                                                title={`${barber.user.firstName}'s break: ${breakItem.label || "Break"}`}
                                            >
                                                <div className="px-2 py-1 w-full">
                                                    <div className="text-xs font-medium text-[var(--text-secondary)] truncate">
                                                        {breakItem.label || "Break"}
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    });
                                })}

                                {/* Booking blocks */}
                                {dayBookings.map((booking) => {
                                    const startTime = booking.startTime.substring(0, 5);
                                    const endTime = booking.endTime.substring(0, 5);
                                    const { top, height } = getBlockStyle(startTime, endTime);
                                    const { className: statusClassName, bgColor, hoverBgColor } = getStatusStyle(booking.status);

                                    return (
                                        <div
                                            key={booking.id}
                                            onClick={() => onBookingClick(booking)}
                                            className={`absolute left-1 right-1 ${statusClassName} rounded cursor-pointer transition-colors overflow-hidden flex items-center`}
                                            style={{
                                                top: top + 2,
                                                height: Math.max(height - 4, 30),
                                                zIndex: 10,
                                                backgroundColor: bgColor,
                                            }}
                                            onMouseEnter={(e) => {
                                                e.currentTarget.style.backgroundColor = hoverBgColor;
                                            }}
                                            onMouseLeave={(e) => {
                                                e.currentTarget.style.backgroundColor = bgColor;
                                            }}
                                        >
                                            <div className="px-2 py-1 w-full">
                                                <div className="text-xs font-bold text-[var(--text-primary)] truncate">
                                                    {booking.customer.firstName} {booking.customer.lastName}
                                                </div>
                                                <div className="text-xs text-[var(--text-muted)] truncate">
                                                    {booking.service.name}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
