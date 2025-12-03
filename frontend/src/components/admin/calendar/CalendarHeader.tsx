interface CalendarHeaderProps {
    weekStart: Date;
    weekEnd: Date;
    onPreviousWeek: () => void;
    onNextWeek: () => void;
    onToday: () => void;
}

export default function CalendarHeader({
    weekStart,
    weekEnd,
    onPreviousWeek,
    onNextWeek,
    onToday,
}: CalendarHeaderProps) {
    const formatDateRange = () => {
        const startMonth = weekStart.toLocaleDateString("en-US", { month: "short" });
        const endMonth = weekEnd.toLocaleDateString("en-US", { month: "short" });
        const startDay = weekStart.getDate();
        const endDay = weekEnd.getDate();
        const year = weekEnd.getFullYear();

        if (startMonth === endMonth) {
            return `${startMonth} ${startDay} - ${endDay}, ${year}`;
        }
        return `${startMonth} ${startDay} - ${endMonth} ${endDay}, ${year}`;
    };

    const isCurrentWeek = () => {
        const today = new Date();
        const currentWeekStart = new Date(today);
        const day = currentWeekStart.getDay();
        const diff = currentWeekStart.getDate() - day + (day === 0 ? -6 : 1);
        currentWeekStart.setDate(diff);
        currentWeekStart.setHours(0, 0, 0, 0);

        return weekStart.getTime() === currentWeekStart.getTime();
    };

    return (
        <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-4">
                <h2 className="text-2xl font-bold text-white">{formatDateRange()}</h2>
            </div>

            <div className="flex items-center gap-2">
                <button
                    onClick={onToday}
                    disabled={isCurrentWeek()}
                    className={`px-4 py-2 rounded-md text-sm font-medium transition ${isCurrentWeek()
                            ? "bg-zinc-800 text-zinc-500 cursor-not-allowed"
                            : "bg-zinc-800 text-white hover:bg-zinc-700"
                        }`}
                >
                    Today
                </button>

                <div className="flex items-center bg-zinc-800 rounded-md">
                    <button
                        onClick={onPreviousWeek}
                        className="p-2 hover:bg-zinc-700 rounded-l-md transition"
                        aria-label="Previous week"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5 text-white"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M15 19l-7-7 7-7"
                            />
                        </svg>
                    </button>
                    <div className="w-px h-6 bg-zinc-700" />
                    <button
                        onClick={onNextWeek}
                        className="p-2 hover:bg-zinc-700 rounded-r-md transition"
                        aria-label="Next week"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5 text-white"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M9 5l7 7-7 7"
                            />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    );
}
