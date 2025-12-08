import type { Barber } from "../../../types";

interface BarberFilterSidebarProps {
    barbers: Barber[];
    selectedBarberId: number | null;
    onSelectBarber: (barberId: number) => void;
}

// Color palette for barbers
const BARBER_COLORS = [
    { bg: "bg-indigo-600", border: "border-indigo-500", text: "text-indigo-400" },
    { bg: "bg-emerald-600", border: "border-emerald-500", text: "text-emerald-400" },
    { bg: "bg-amber-600", border: "border-amber-500", text: "text-amber-400" },
    { bg: "bg-rose-600", border: "border-rose-500", text: "text-rose-400" },
    { bg: "bg-cyan-600", border: "border-cyan-500", text: "text-cyan-400" },
    { bg: "bg-purple-600", border: "border-purple-500", text: "text-purple-400" },
    { bg: "bg-orange-600", border: "border-orange-500", text: "text-orange-400" },
    { bg: "bg-teal-600", border: "border-teal-500", text: "text-teal-400" },
];

export const getBarberColor = (index: number) => {
    return BARBER_COLORS[index % BARBER_COLORS.length];
};

export default function BarberFilterSidebar({
    barbers,
    selectedBarberId,
    onSelectBarber,
}: BarberFilterSidebarProps) {
    return (
        <div className="w-64 flex-shrink-0">
            <div className="bg-zinc-900 rounded-lg border border-zinc-800 p-4">
                <h3 className="text-sm font-semibold text-white mb-4">Select Employee</h3>

                <div className="space-y-2">
                    {barbers.map((barber, index) => {
                        const color = getBarberColor(index);
                        const isSelected = selectedBarberId === barber.id;
                        const initials = `${barber.user.firstName.charAt(0)}${barber.user.lastName.charAt(0)}`.toUpperCase();

                        return (
                            <button
                                key={barber.id}
                                onClick={() => onSelectBarber(barber.id)}
                                className={`w-full flex items-center gap-3 p-3 rounded-lg cursor-pointer transition ${isSelected
                                    ? `bg-zinc-800 border-2 ${color.border}`
                                    : "bg-zinc-900 border border-zinc-700 hover:bg-zinc-800/50 hover:border-zinc-600"
                                    }`}
                            >
                                {/* Profile picture or initials placeholder */}
                                {barber.profileImageUrl ? (
                                    <img
                                        src={barber.profileImageUrl}
                                        alt={`${barber.user.firstName} ${barber.user.lastName}`}
                                        className="w-10 h-10 rounded-full object-cover flex-shrink-0"
                                    />
                                ) : (
                                    <div
                                        className={`w-10 h-10 rounded-full ${color.bg} flex-shrink-0 flex items-center justify-center text-white font-semibold text-sm`}
                                    >
                                        {initials}
                                    </div>
                                )}
                                <span
                                    className={`text-sm truncate ${isSelected ? "text-white font-medium" : "text-zinc-400"
                                        }`}
                                >
                                    {barber.user.firstName} {barber.user.lastName}
                                </span>
                            </button>
                        );
                    })}
                </div>

                {barbers.length === 0 && (
                    <p className="text-sm text-zinc-500 text-center py-4">
                        No barbers found
                    </p>
                )}
            </div>

            {/* Legend */}
            <div className="mt-4 bg-zinc-900 rounded-lg border border-zinc-800 p-4">
                <h3 className="text-sm font-semibold text-white mb-3">Legend</h3>
                <div className="space-y-2 text-xs">
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-zinc-700 border-2 border-dashed border-zinc-500" />
                        <span className="text-zinc-400">Break Time</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-green-600/30 border border-green-500" />
                        <span className="text-zinc-400">Confirmed</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-yellow-600/30 border border-yellow-500" />
                        <span className="text-zinc-400">Pending</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-zinc-600/30 border border-zinc-500" />
                        <span className="text-zinc-400">Completed</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
