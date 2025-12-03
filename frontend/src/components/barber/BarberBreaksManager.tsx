import { useState, useEffect } from "react";
import { barberBreaksApi } from "../../api/endpoints";
import type { BarberBreak } from "../../types";

interface BarberBreaksManagerProps {
    barberId?: number;
    /** Optional: earliest working hour across all days (for validation) */
    workStartTime?: string;
    /** Optional: latest working hour across all days (for validation) */
    workEndTime?: string;
}

/**
 * Breaks Manager - Allows barbers to manage their breaks (e.g., lunch)
 * Breaks apply to all working days
 */
export default function BarberBreaksManager({
    barberId,
    workStartTime = "06:00",
    workEndTime = "22:00",
}: BarberBreaksManagerProps) {
    const [breaks, setBreaks] = useState<BarberBreak[]>([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [editingBreak, setEditingBreak] = useState<BarberBreak | null>(null);

    // Form state for new/edit break
    const [formData, setFormData] = useState({
        startTime: "12:00",
        endTime: "13:00",
        label: "",
    });

    // Generate time options from 6:00 AM to 10:00 PM in 15-minute intervals
    const timeOptions: { value: string; label: string }[] = [];
    for (let hour = 6; hour <= 22; hour++) {
        for (let minute = 0; minute < 60; minute += 15) {
            const timeStr = `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
            const displayTime = new Date(`2000-01-01T${timeStr}`).toLocaleTimeString(
                "en-US",
                {
                    hour: "numeric",
                    minute: "2-digit",
                    hour12: true,
                }
            );
            timeOptions.push({ value: timeStr, label: displayTime });
        }
    }

    // Load existing breaks
    useEffect(() => {
        if (!barberId) return;
        loadBreaks();
    }, [barberId]);

    const loadBreaks = async () => {
        if (!barberId) return;
        setLoading(true);
        try {
            const response = await barberBreaksApi.getByBarberId(barberId);
            // Normalize time format (remove seconds if present)
            const normalizedBreaks = response.data.map((b: any) => ({
                ...b,
                startTime: b.startTime.substring(0, 5),
                endTime: b.endTime.substring(0, 5),
            }));
            setBreaks(normalizedBreaks);
            setError(null);
        } catch (err) {
            console.error("Failed to load breaks", err);
            setError("Failed to load breaks");
        } finally {
            setLoading(false);
        }
    };

    const formatTimeForDisplay = (time: string) => {
        return new Date(`2000-01-01T${time}`).toLocaleTimeString("en-US", {
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
        });
    };

    const validateBreak = (start: string, end: string): string | null => {
        if (start >= end) {
            return "End time must be after start time";
        }
        if (start < workStartTime || end > workEndTime) {
            return `Break must be within working hours (${formatTimeForDisplay(workStartTime)} - ${formatTimeForDisplay(workEndTime)})`;
        }
        return null;
    };

    const handleAddBreak = async () => {
        if (!barberId) return;

        const validationError = validateBreak(formData.startTime, formData.endTime);
        if (validationError) {
            setError(validationError);
            return;
        }

        setSaving(true);
        setError(null);
        try {
            await barberBreaksApi.create({
                barberId,
                startTime: formData.startTime,
                endTime: formData.endTime,
                label: formData.label || undefined,
            });
            setShowAddForm(false);
            setFormData({ startTime: "12:00", endTime: "13:00", label: "" });
            loadBreaks();
        } catch (err: any) {
            const message = err.response?.data?.message || "Failed to add break";
            setError(message);
        } finally {
            setSaving(false);
        }
    };

    const handleUpdateBreak = async () => {
        if (!editingBreak) return;

        const validationError = validateBreak(formData.startTime, formData.endTime);
        if (validationError) {
            setError(validationError);
            return;
        }

        setSaving(true);
        setError(null);
        try {
            await barberBreaksApi.update(editingBreak.id, {
                startTime: formData.startTime,
                endTime: formData.endTime,
                label: formData.label || undefined,
            });
            setEditingBreak(null);
            setFormData({ startTime: "12:00", endTime: "13:00", label: "" });
            loadBreaks();
        } catch (err: any) {
            const message = err.response?.data?.message || "Failed to update break";
            setError(message);
        } finally {
            setSaving(false);
        }
    };

    const handleDeleteBreak = async (breakId: number) => {
        setSaving(true);
        setError(null);
        try {
            await barberBreaksApi.delete(breakId);
            loadBreaks();
        } catch (err: any) {
            const message = err.response?.data?.message || "Failed to delete break";
            setError(message);
        } finally {
            setSaving(false);
        }
    };

    const startEditing = (breakItem: BarberBreak) => {
        setEditingBreak(breakItem);
        setFormData({
            startTime: breakItem.startTime,
            endTime: breakItem.endTime,
            label: breakItem.label || "",
        });
        setShowAddForm(false);
    };

    const cancelForm = () => {
        setShowAddForm(false);
        setEditingBreak(null);
        setFormData({ startTime: "12:00", endTime: "13:00", label: "" });
        setError(null);
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center py-8">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-indigo-500"></div>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <div className="flex items-center justify-between">
                <div>
                    <h3 className="text-lg font-semibold text-white">Breaks</h3>
                    <p className="text-xs text-zinc-500">
                        Breaks apply to all working days
                    </p>
                </div>
                {!showAddForm && !editingBreak && (
                    <button
                        onClick={() => setShowAddForm(true)}
                        className="text-sm bg-emerald-600 hover:bg-emerald-500 text-white px-3 py-1.5 rounded-md transition"
                    >
                        + Add Break
                    </button>
                )}
            </div>

            {error && (
                <div className="p-3 bg-red-900/50 border border-red-800 rounded text-red-300 text-sm">
                    {error}
                </div>
            )}

            {/* Add/Edit Break Form */}
            {(showAddForm || editingBreak) && (
                <div className="p-4 bg-zinc-800 rounded-lg border border-zinc-700">
                    <h4 className="text-sm font-medium text-white mb-3">
                        {editingBreak ? "Edit Break" : "Add New Break"}
                    </h4>

                    <div className="space-y-3">
                        <div>
                            <label className="block text-xs font-medium text-zinc-400 mb-1">
                                Label (optional)
                            </label>
                            <input
                                type="text"
                                value={formData.label}
                                onChange={(e) =>
                                    setFormData({ ...formData, label: e.target.value })
                                }
                                placeholder="e.g., Lunch, Coffee break"
                                className="w-full px-3 py-2 border border-zinc-600 rounded-md bg-zinc-900 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
                            />
                        </div>

                        <div className="flex items-center gap-3">
                            <div className="flex-1">
                                <label className="block text-xs font-medium text-zinc-400 mb-1">
                                    Start Time
                                </label>
                                <select
                                    value={formData.startTime}
                                    onChange={(e) =>
                                        setFormData({ ...formData, startTime: e.target.value })
                                    }
                                    className="w-full px-3 py-2 border border-zinc-600 rounded-md bg-zinc-900 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
                                >
                                    {timeOptions.map((time) => (
                                        <option key={time.value} value={time.value}>
                                            {time.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="text-zinc-500 pt-5">→</div>

                            <div className="flex-1">
                                <label className="block text-xs font-medium text-zinc-400 mb-1">
                                    End Time
                                </label>
                                <select
                                    value={formData.endTime}
                                    onChange={(e) =>
                                        setFormData({ ...formData, endTime: e.target.value })
                                    }
                                    className="w-full px-3 py-2 border border-zinc-600 rounded-md bg-zinc-900 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
                                >
                                    {timeOptions.map((time) => (
                                        <option key={time.value} value={time.value}>
                                            {time.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="flex gap-2 pt-2">
                            <button
                                onClick={editingBreak ? handleUpdateBreak : handleAddBreak}
                                disabled={saving}
                                className="flex-1 bg-emerald-600 hover:bg-emerald-500 disabled:bg-zinc-700 text-white py-2 rounded-md transition text-sm font-medium"
                            >
                                {saving
                                    ? "Saving..."
                                    : editingBreak
                                        ? "Update Break"
                                        : "Add Break"}
                            </button>
                            <button
                                onClick={cancelForm}
                                className="px-4 bg-zinc-700 hover:bg-zinc-600 text-zinc-300 py-2 rounded-md transition text-sm"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Breaks List */}
            {breaks.length === 0 ? (
                <div className="text-center py-6 text-zinc-500 text-sm">
                    No breaks scheduled. Add a break for lunch or other regular breaks.
                </div>
            ) : (
                <div className="space-y-2">
                    {breaks.map((breakItem) => (
                        <div
                            key={breakItem.id}
                            className="flex items-center justify-between p-3 bg-zinc-800/50 rounded-lg border border-zinc-700"
                        >
                            <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-emerald-600/20 flex items-center justify-center">
                                    <svg
                                        xmlns="http://www.w3.org/2000/svg"
                                        className="h-4 w-4 text-emerald-400"
                                        fill="none"
                                        viewBox="0 0 24 24"
                                        stroke="currentColor"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                                        />
                                    </svg>
                                </div>
                                <div>
                                    <p className="text-white font-medium text-sm">
                                        {formatTimeForDisplay(breakItem.startTime)} -{" "}
                                        {formatTimeForDisplay(breakItem.endTime)}
                                    </p>
                                    {breakItem.label && (
                                        <p className="text-xs text-zinc-400">{breakItem.label}</p>
                                    )}
                                </div>
                            </div>

                            <div className="flex gap-2">
                                <button
                                    onClick={() => startEditing(breakItem)}
                                    className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDeleteBreak(breakItem.id)}
                                    disabled={saving}
                                    className="text-red-400 hover:text-red-300 text-sm font-medium"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
