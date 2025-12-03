import type { Barber } from "../../../types";

interface BarberFilterSidebarProps {
    barbers: Barber[];
    selectedBarberIds: Set<number>;
    onToggleBarber: (barberId: number) => void;
    onSelectAll: () => void;
    onDeselectAll: () => void;
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
    selectedBarberIds,
    onToggleBarber,
    onSelectAll,
    onDeselectAll,
}: BarberFilterSidebarProps) {
    const allSelected = selectedBarberIds.size === barbers.length;
    const noneSelected = selectedBarberIds.size === 0;

    return (
        <div className="w-64 flex-shrink-0">
            <div className="bg-zinc-900 rounded-lg border border-zinc-800 p-4">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-sm font-semibold text-white">Barbers</h3>
                    <div className="flex gap-2">
                        <button
                            onClick={onSelectAll}
                            disabled={allSelected}
                            className={`text-xs ${allSelected
                                    ? "text-zinc-600"
                                    : "text-indigo-400 hover:text-indigo-300"
                                }`}
                        >
                            All
                        </button>
                        <span className="text-zinc-600">|</span>
                        <button
                            onClick={onDeselectAll}
                            disabled={noneSelected}
                            className={`text-xs ${noneSelected
                                    ? "text-zinc-600"
                                    : "text-indigo-400 hover:text-indigo-300"
                                }`}
                        >
                            None
                        </button>
                    </div>
                </div>

                <div className="space-y-2">
                    {barbers.map((barber, index) => {
                        const color = getBarberColor(index);
                        const isSelected = selectedBarberIds.has(barber.id);

                        return (
                            <label
                                key={barber.id}
                                className={`flex items-center gap-3 p-2 rounded-md cursor-pointer transition ${isSelected
                                        ? "bg-zinc-800"
                                        : "bg-zinc-900 hover:bg-zinc-800/50"
                                    }`}
                            >
                                <input
                                    type="checkbox"
                                    checked={isSelected}
                                    onChange={() => onToggleBarber(barber.id)}
                                    className="sr-only"
                                />
                                <div
                                    className={`w-4 h-4 rounded ${isSelected ? color.bg : "bg-zinc-700"
                                        } flex items-center justify-center transition`}
                                >
                                    {isSelected && (
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            className="h-3 w-3 text-white"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={3}
                                                d="M5 13l4 4L19 7"
                                            />
                                        </svg>
                                    )}
                                </div>
                                <div className="flex items-center gap-2 flex-1 min-w-0">
                                    <div
                                        className={`w-3 h-3 rounded-full ${color.bg} flex-shrink-0`}
                                    />
                                    <span
                                        className={`text-sm truncate ${isSelected ? "text-white" : "text-zinc-500"
                                            }`}
                                    >
                                        {barber.user.firstName} {barber.user.lastName}
                                    </span>
                                </div>
                            </label>
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
