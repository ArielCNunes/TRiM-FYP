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
        } catch {
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
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-[var(--focus-ring)]"></div>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <div className="flex items-center justify-between">
                <div>
                    <h3 className="text-lg font-semibold text-[var(--text-primary)]">Breaks</h3>
                    <p className="text-xs text-[var(--text-subtle)]">
                        Breaks apply to all working days
                    </p>
                </div>
                {!showAddForm && !editingBreak && (
                    <button
                        onClick={() => setShowAddForm(true)}
                        className="text-sm bg-[var(--success)] hover:bg-[var(--success-hover)] text-[var(--text-primary)] px-3 py-1.5 rounded-md transition"
                    >
                        + Add Break
                    </button>
                )}
            </div>

            {error && (
                <div className="p-3 bg-[var(--danger-muted)] border border-[var(--danger-border)] rounded text-[var(--danger-text-light)] text-sm">
                    {error}
                </div>
            )}

            {/* Add/Edit Break Form */}
            {(showAddForm || editingBreak) && (
                <div className="p-4 bg-[var(--bg-elevated)] rounded-lg border border-[var(--border-default)]">
                    <h4 className="text-sm font-medium text-[var(--text-primary)] mb-3">
                        {editingBreak ? "Edit Break" : "Add New Break"}
                    </h4>

                    <div className="space-y-3">
                        <div>
                            <label className="block text-xs font-medium text-[var(--text-muted)] mb-1">
                                Label (optional)
                            </label>
                            <input
                                type="text"
                                value={formData.label}
                                onChange={(e) =>
                                    setFormData({ ...formData, label: e.target.value })
                                }
                                placeholder="e.g., Lunch, Coffee break"
                                className="w-full px-3 py-2 border border-[var(--border-strong)] rounded-md bg-[var(--bg-surface)] text-[var(--text-primary)] focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
                            />
                        </div>

                        <div className="flex items-center gap-3">
                            <div className="flex-1">
                                <label className="block text-xs font-medium text-[var(--text-muted)] mb-1">
                                    Start Time
                                </label>
                                <select
                                    value={formData.startTime}
                                    onChange={(e) =>
                                        setFormData({ ...formData, startTime: e.target.value })
                                    }
                                    className="w-full px-3 py-2 border border-[var(--border-strong)] rounded-md bg-[var(--bg-surface)] text-[var(--text-primary)] focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
                                >
                                    {timeOptions.map((time) => (
                                        <option key={time.value} value={time.value}>
                                            {time.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="text-[var(--text-subtle)] pt-5">→</div>

                            <div className="flex-1">
                                <label className="block text-xs font-medium text-[var(--text-muted)] mb-1">
                                    End Time
                                </label>
                                <select
                                    value={formData.endTime}
                                    onChange={(e) =>
                                        setFormData({ ...formData, endTime: e.target.value })
                                    }
                                    className="w-full px-3 py-2 border border-[var(--border-strong)] rounded-md bg-[var(--bg-surface)] text-[var(--text-primary)] focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm"
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
                                className="flex-1 bg-[var(--success)] hover:bg-[var(--success-hover)] disabled:bg-[var(--bg-muted)] text-[var(--text-primary)] py-2 rounded-md transition text-sm font-medium"
                            >
                                {saving
                                    ? "Saving..."
                                    : editingBreak
                                        ? "Update Break"
                                        : "Add Break"}
                            </button>
                            <button
                                onClick={cancelForm}
                                className="px-4 bg-[var(--bg-muted)] hover:bg-[var(--bg-subtle)] text-[var(--text-secondary)] py-2 rounded-md transition text-sm"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Breaks List */}
            {breaks.length === 0 ? (
                <div className="text-center py-6 text-[var(--text-subtle)] text-sm">
                    No breaks scheduled. Add a break for lunch or other regular breaks.
                </div>
            ) : (
                <div className="space-y-2">
                    {breaks.map((breakItem) => (
                        <div
                            key={breakItem.id}
                            className="flex items-center justify-between p-3 bg-[var(--bg-elevated)]/50 rounded-lg border border-[var(--border-default)]"
                        >
                            <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-[var(--success)]/20 flex items-center justify-center">
                                    <svg
                                        xmlns="http://www.w3.org/2000/svg"
                                        className="h-4 w-4 text-[var(--success-text)]"
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
                                    <p className="text-[var(--text-primary)] font-medium text-sm">
                                        {formatTimeForDisplay(breakItem.startTime)} -{" "}
                                        {formatTimeForDisplay(breakItem.endTime)}
                                    </p>
                                    {breakItem.label && (
                                        <p className="text-xs text-[var(--text-muted)]">{breakItem.label}</p>
                                    )}
                                </div>
                            </div>

                            <div className="flex gap-2">
                                <button
                                    onClick={() => startEditing(breakItem)}
                                    className="text-[var(--accent-text)] hover:text-[var(--accent-text-light)] text-sm font-medium"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDeleteBreak(breakItem.id)}
                                    disabled={saving}
                                    className="text-[var(--danger-text)] hover:text-[var(--danger-text-light)] text-sm font-medium"
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
