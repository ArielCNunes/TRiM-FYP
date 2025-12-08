import { useState, useEffect, useMemo } from "react";
import { bookingsApi, barbersApi, barberBreaksApi } from "../../api/endpoints";
import type { BookingResponse, Barber, BarberBreak } from "../../types";
import { useAppSelector } from "../../store/hooks";
import CalendarHeader from "../admin/calendar/CalendarHeader";
import CalendarGrid from "../admin/calendar/CalendarGrid";
import BookingDetailModal from "../admin/calendar/BookingDetailModal";

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

export default function BarberCalendar() {
    const user = useAppSelector((state) => state.auth.user);
    const barberId = user?.barberId;

    const [currentWeekStart, setCurrentWeekStart] = useState<Date>(
        getWeekStart(new Date())
    );
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [barber, setBarber] = useState<Barber | null>(null);
    const [barberBreaks, setBarberBreaks] = useState<Map<number, BarberBreak[]>>(
        new Map()
    );
    const [loading, setLoading] = useState(true);
    const [selectedBooking, setSelectedBooking] =
        useState<BookingResponse | null>(null);

    const weekDates = useMemo(
        () => getWeekDates(currentWeekStart),
        [currentWeekStart]
    );

    // Load barber info on mount
    useEffect(() => {
        if (barberId) {
            loadBarberInfo();
        }
    }, [barberId]);

    // Load bookings when week changes
    useEffect(() => {
        if (barberId) {
            loadBookingsForWeek();
        }
    }, [currentWeekStart, barberId]);

    // Load breaks when barber is loaded
    useEffect(() => {
        if (barber) {
            loadBarberBreaks();
        }
    }, [barber]);

    const loadBarberInfo = async () => {
        if (!barberId) return;
        try {
            const response = await barbersApi.getById(barberId);
            setBarber(response.data);
        } catch (error) {
            console.error("Failed to load barber info", error);
        }
    };

    const loadBookingsForWeek = async () => {
        if (!barberId) return;
        setLoading(true);
        try {
            // Fetch all bookings for this barber
            const response = await bookingsApi.getBarberBookings(barberId);
            const allBookings = response.data as BookingResponse[];

            // Filter to only bookings within the current week
            const weekStart = formatDate(currentWeekStart);
            const weekEnd = formatDate(addDays(currentWeekStart, 6));

            const weekBookings = allBookings.filter((booking) => {
                return booking.bookingDate >= weekStart && booking.bookingDate <= weekEnd;
            });

            setBookings(weekBookings);
        } catch (error) {
            console.error("Failed to load bookings", error);
        } finally {
            setLoading(false);
        }
    };

    const loadBarberBreaks = async () => {
        if (!barberId) return;
        try {
            const response = await barberBreaksApi.getByBarberId(barberId);
            const breaksMap = new Map<number, BarberBreak[]>();
            breaksMap.set(barberId, response.data);
            setBarberBreaks(breaksMap);
        } catch (error) {
            console.error("Failed to load barber breaks", error);
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

    if (!barberId) {
        return (
            <div className="flex justify-center items-center py-20 text-zinc-400">
                Unable to load barber information
            </div>
        );
    }

    return (
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
                    <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
                </div>
            ) : barber ? (
                <CalendarGrid
                    weekDates={weekDates}
                    bookings={bookings}
                    barbers={[barber]}
                    barberBreaks={barberBreaks}
                    onBookingClick={setSelectedBooking}
                />
            ) : (
                <div className="flex justify-center items-center py-20 text-zinc-400">
                    Loading your schedule...
                </div>
            )}

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
