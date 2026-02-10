import { useState, useEffect, useMemo } from "react";
import { bookingsApi, barbersApi, barberBreaksApi } from "../../../api/endpoints";
import type { BookingResponse, Barber, BarberBreak } from "../../../types";
import CalendarHeader from "./CalendarHeader";
import CalendarGrid from "./CalendarGrid";
import BarberFilterSidebar from "./BarberFilterSidebar";
import BookingDetailModal from "./BookingDetailModal";

// Helper to get the start of the week (Monday)
const getWeekStart = (date: Date): Date => {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Sunday
    d.setDate(diff);
    d.setHours(0, 0, 0, 0);
    return d;
};

// Helper to format date as YYYY-MM-DD
const formatDate = (date: Date): string => {
    return date.toISOString().split("T")[0];
};

// Helper to add days to a date
const addDays = (date: Date, days: number): Date => {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
};

// Generate array of dates for the week
const getWeekDates = (weekStart: Date): Date[] => {
    return Array.from({ length: 7 }, (_, i) => addDays(weekStart, i));
};

export default function AdminCalendar() {
    const [currentWeekStart, setCurrentWeekStart] = useState<Date>(
        getWeekStart(new Date())
    );
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [barbers, setBarbers] = useState<Barber[]>([]);
    const [barberBreaks, setBarberBreaks] = useState<Map<number, BarberBreak[]>>(
        new Map()
    );
    const [selectedBarberId, setSelectedBarberId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [selectedBooking, setSelectedBooking] =
        useState<BookingResponse | null>(null);

    const weekDates = useMemo(
        () => getWeekDates(currentWeekStart),
        [currentWeekStart]
    );

    // Load barbers on mount
    useEffect(() => {
        loadBarbers();
    }, []);

    // Load bookings when week changes
    useEffect(() => {
        loadBookingsForWeek();
    }, [currentWeekStart]);

    // Load breaks when barbers are loaded
    useEffect(() => {
        if (barbers.length > 0) {
            loadAllBarberBreaks();
        }
    }, [barbers]);

    const loadBarbers = async () => {
        try {
            const response = await barbersApi.getActive();
            setBarbers(response.data);
            // Select first barber by default
            if (response.data.length > 0) {
                setSelectedBarberId(response.data[0].id);
            }
        } catch {
            // Error handled silently - barbers will remain empty
        }
    };

    const loadBookingsForWeek = async () => {
        setLoading(true);
        try {
            // Fetch bookings for each day of the week
            const allBookings: BookingResponse[] = [];
            for (const date of weekDates) {
                const response = await bookingsApi.getAll({ date: formatDate(date) });
                allBookings.push(...response.data);
            }
            // Filter out cancelled bookings - they should not be displayed in the calendar
            const activeBookings = allBookings.filter(booking => booking.status !== "CANCELLED");
            setBookings(activeBookings);
        } catch {
            // Error handled silently - bookings will remain empty
        } finally {
            setLoading(false);
        }
    };

    const loadAllBarberBreaks = async () => {
        try {
            const breaksMap = new Map<number, BarberBreak[]>();
            for (const barber of barbers) {
                const response = await barberBreaksApi.getByBarberId(barber.id);
                breaksMap.set(barber.id, response.data);
            }
            setBarberBreaks(breaksMap);
        } catch {
            // Error handled silently - breaks will remain empty
        }
    };

    // Navigation handlers
    const goToPreviousWeek = () => {
        setCurrentWeekStart(addDays(currentWeekStart, -7));
    };

    const goToNextWeek = () => {
        setCurrentWeekStart(addDays(currentWeekStart, 7));
    };

    const goToToday = () => {
        setCurrentWeekStart(getWeekStart(new Date()));
    };

    // Barber selection handler
    const selectBarber = (barberId: number) => {
        setSelectedBarberId(barberId);
    };

    // Filter bookings by selected barber
    const filteredBookings = useMemo(() => {
        if (selectedBarberId === null) return [];
        return bookings.filter((booking) =>
            booking.barber.id === selectedBarberId
        );
    }, [bookings, selectedBarberId]);

    // Get selected barber for display
    const selectedBarber = useMemo(() => {
        return barbers.find(b => b.id === selectedBarberId) || null;
    }, [barbers, selectedBarberId]);

    return (
        <div className="flex gap-6">
            {/* Sidebar */}
            <BarberFilterSidebar
                barbers={barbers}
                selectedBarberId={selectedBarberId}
                onSelectBarber={selectBarber}
            />

            {/* Main Calendar Area */}
            <div className="flex-1 min-w-0">
                <CalendarHeader
                    weekStart={currentWeekStart}
                    weekEnd={addDays(currentWeekStart, 6)}
                    onPreviousWeek={goToPreviousWeek}
                    onNextWeek={goToNextWeek}
                    onToday={goToToday}
                />

                {loading ? (
                    <div className="flex justify-center items-center py-20">
                        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--focus-ring)]"></div>
                    </div>
                ) : selectedBarber ? (
                    <CalendarGrid
                        weekDates={weekDates}
                        bookings={filteredBookings}
                        barbers={[selectedBarber]}
                        barberBreaks={barberBreaks}
                        onBookingClick={setSelectedBooking}
                    />
                ) : (
                    <div className="flex justify-center items-center py-20 text-[var(--text-muted)]">
                        Select an employee to view their schedule
                    </div>
                )}
            </div>

            {/* Booking Detail Modal */}
            {selectedBooking && (
                <BookingDetailModal
                    booking={selectedBooking}
                    onClose={() => setSelectedBooking(null)}
                    canChangeStatus={true}
                    onStatusChange={() => {
                        loadBookingsForWeek();
                    }}
                />
            )}
        </div>
    );
}
