import { useState } from "react";
import type { Customer } from "../../../types";

interface BlacklistModalProps {
    customer: Customer;
    onConfirm: (reason: string) => void;
    onCancel: () => void;
}

export default function BlacklistModal({
    customer,
    onConfirm,
    onCancel,
}: BlacklistModalProps) {
    const [reason, setReason] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!reason.trim()) {
            setError("Please provide a reason for blacklisting");
            return;
        }

        if (reason.length > 500) {
            setError("Reason must be 500 characters or less");
            return;
        }

        onConfirm(reason.trim());
    };

    return (
        <div className="fixed inset-0 bg-[var(--overlay)] flex items-center justify-center z-50 p-4">
            <div className="bg-[var(--bg-surface)] rounded-xl border border-[var(--border-subtle)] max-w-md w-full shadow-2xl">
                {/* Header */}
                <div className="px-6 py-4 border-b border-[var(--border-subtle)]">
                    <h3 className="text-lg font-semibold text-[var(--text-primary)]">
                        Blacklist Customer
                    </h3>
                    <p className="text-sm text-[var(--text-muted)] mt-1">
                        {customer.firstName} {customer.lastName} ({customer.email})
                    </p>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit}>
                    <div className="px-6 py-4">
                        <div className="bg-amber-500/10 border border-amber-500/30 rounded-lg p-3 mb-4">
                            <p className="text-sm text-amber-400">
                                <strong>Warning:</strong> Blacklisted customers will not be able
                                to make new bookings.
                            </p>
                        </div>

                        <label className="block text-sm font-medium text-[var(--text-secondary)] mb-2">
                            Reason for blacklisting <span className="text-[var(--danger-text)]">*</span>
                        </label>
                        <textarea
                            value={reason}
                            onChange={(e) => {
                                setReason(e.target.value);
                                setError("");
                            }}
                            placeholder="e.g., Multiple no-shows without notice"
                            rows={3}
                            maxLength={500}
                            className="w-full px-3 py-2 bg-[var(--bg-elevated)] border border-[var(--border-default)] rounded-lg text-[var(--text-primary)] placeholder-[var(--text-subtle)] focus:outline-none focus:ring-2 focus:ring-[var(--focus-ring)] resize-none"
                        />
                        <div className="flex justify-between mt-1">
                            {error ? (
                                <p className="text-sm text-[var(--danger-text)]">{error}</p>
                            ) : (
                                <span />
                            )}
                            <p className="text-xs text-[var(--text-subtle)]">{reason.length}/500</p>
                        </div>

                        {/* No-show count info */}
                        {customer.noShowCount > 0 && (
                            <p className="text-sm text-[var(--text-muted)] mt-3">
                                This customer has{" "}
                                <span className="text-amber-400 font-medium">
                                    {customer.noShowCount} no-show
                                    {customer.noShowCount !== 1 ? "s" : ""}
                                </span>{" "}
                                on record.
                            </p>
                        )}
                    </div>

                    {/* Actions */}
                    <div className="px-6 py-4 border-t border-[var(--border-subtle)] flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 text-sm font-medium text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 text-sm font-medium bg-[var(--danger)] text-[var(--text-primary)] rounded-lg hover:bg-[var(--danger-hover)] transition-colors"
                        >
                            Blacklist Customer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
