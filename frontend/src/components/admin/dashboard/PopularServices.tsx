import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from "recharts";
import type { DashboardStats } from "../../../types";

interface PopularServicesProps {
    popularServices: DashboardStats["popularServices"];
}

const COLORS = [
    "var(--accent)",
    "var(--success-text)",
    "var(--warning-text)",
    "var(--purple-text)",
    "var(--danger-text)",
];

export default function PopularServices({ popularServices }: PopularServicesProps) {
    if (!popularServices || popularServices.length === 0) {
        return (
            <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
                <h2 className="text-xl font-bold text-[var(--text-primary)] mb-4">
                    Popular Services
                </h2>
                <p className="text-[var(--text-muted)]">No service data available yet.</p>
            </div>
        );
    }

    const total = popularServices.reduce((sum, s) => sum + s.count, 0);

    const data = popularServices.map((service, i) => ({
        name: service.name,
        value: service.count,
        color: COLORS[i % COLORS.length],
    }));

    return (
        <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
            <h2 className="text-xl font-bold text-[var(--text-primary)] mb-6">
                Popular Services
            </h2>
            <div className="flex flex-col sm:flex-row items-center sm:items-start gap-8">
                {/* Pie Chart */}
                <div className="w-52 h-52 shrink-0">
                    <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                            <Pie
                                data={data}
                                cx="50%"
                                cy="50%"
                                innerRadius={45}
                                outerRadius={90}
                                paddingAngle={2}
                                dataKey="value"
                                animationDuration={600}
                                stroke="none"
                            >
                                {data.map((entry) => (
                                    <Cell key={entry.name} fill={entry.color} />
                                ))}
                            </Pie>
                            <Tooltip
                                content={({ active, payload }) => {
                                    if (!active || !payload?.length) return null;
                                    const item = payload[0];
                                    const pct = total > 0 ? ((item.value as number) / total) * 100 : 0;
                                    return (
                                        <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-lg px-3 py-2 shadow-lg">
                                            <p className="text-sm font-medium text-[var(--text-primary)]">
                                                {item.name}
                                            </p>
                                            <p className="text-xs text-[var(--text-muted)]">
                                                {(item.value as number).toLocaleString()} bookings ({pct.toFixed(1)}%)
                                            </p>
                                        </div>
                                    );
                                }}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                {/* Legend */}
                <div className="flex-1 space-y-3">
                    {data.map((entry) => {
                        const pct = total > 0 ? (entry.value / total) * 100 : 0;
                        return (
                            <div key={entry.name} className="flex items-center gap-3">
                                <span
                                    className="w-3.5 h-3.5 rounded-sm shrink-0"
                                    style={{ backgroundColor: entry.color }}
                                />
                                <div className="flex-1 flex justify-between items-center min-w-0 gap-2">
                                    <span className="text-sm font-medium text-[var(--text-primary)] truncate">
                                        {entry.name}
                                    </span>
                                    <span className="text-sm text-[var(--text-muted)] whitespace-nowrap">
                                        {entry.value.toLocaleString()} ({pct.toFixed(1)}%)
                                    </span>
                                </div>
                            </div>
                        );
                    })}
                    <p className="text-xs text-[var(--text-muted)] pt-2 border-t border-[var(--border-subtle)]">
                        Total: {total.toLocaleString()} bookings
                    </p>
                </div>
            </div>
        </div>
    );
}
