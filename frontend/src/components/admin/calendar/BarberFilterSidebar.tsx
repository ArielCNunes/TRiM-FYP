import type { Barber } from "../../../types";

interface BarberFilterSidebarProps {
    barbers: Barber[];
    selectedBarberId: number | null;
    onSelectBarber: (barberId: number) => void;
}

// Color palette for barbers
const BARBER_COLORS = [
    { bg: "bg-[var(--accent)]", border: "border-[var(--focus-ring)]", text: "text-[var(--accent-text)]" },
    { bg: "bg-[var(--success)]", border: "border-emerald-500", text: "text-[var(--success-text)]" },
    { bg: "bg-amber-600", border: "border-amber-500", text: "text-[var(--warning-text)]" },
    { bg: "bg-rose-600", border: "border-rose-500", text: "text-rose-400" },
    { bg: "bg-cyan-600", border: "border-cyan-500", text: "text-cyan-400" },
    { bg: "bg-[var(--purple)]", border: "border-purple-500", text: "text-[var(--purple-text)]" },
    { bg: "bg-[var(--orange)]", border: "border-orange-500", text: "text-[var(--orange-text)]" },
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
            <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-4">
                <h3 className="text-sm font-semibold text-[var(--text-primary)] mb-4">Select Employee</h3>

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
                                    ? "bg-[var(--bg-elevated)] border border-[var(--border-strong)]"
                                    : "bg-[var(--bg-surface)] border border-[var(--border-default)] hover:bg-[var(--bg-elevated)]/50 hover:border-[var(--border-strong)]"
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
                                        className={`w-10 h-10 rounded-full ${color.bg} flex-shrink-0 flex items-center justify-center text-[var(--text-primary)] font-semibold text-sm`}
                                    >
                                        {initials}
                                    </div>
                                )}
                                <span
                                    className={`text-sm truncate ${isSelected ? "text-[var(--text-primary)] font-medium" : "text-[var(--text-muted)]"
                                        }`}
                                >
                                    {barber.user.firstName} {barber.user.lastName}
                                </span>
                            </button>
                        );
                    })}
                </div>

                {barbers.length === 0 && (
                    <p className="text-sm text-[var(--text-subtle)] text-center py-4">
                        No barbers found
                    </p>
                )}
            </div>

            {/* Legend */}
            <div className="mt-4 bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-4">
                <h3 className="text-sm font-semibold text-[var(--text-primary)] mb-3">Legend</h3>
                <div className="space-y-2 text-xs">
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-[var(--bg-muted)] border-2 border-dashed border-zinc-500" />
                        <span className="text-[var(--text-muted)]">Break Time</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-[var(--green)]/30 border border-green-500" />
                        <span className="text-[var(--text-muted)]">Confirmed</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-yellow-600/30 border border-yellow-500" />
                        <span className="text-[var(--text-muted)]">Pending</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-[var(--bg-subtle)]/30 border border-zinc-500" />
                        <span className="text-[var(--text-muted)]">Completed</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
