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
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
            <div className="bg-zinc-900 rounded-xl border border-zinc-800 max-w-md w-full shadow-2xl">
                {/* Header */}
                <div className="px-6 py-4 border-b border-zinc-800">
                    <h3 className="text-lg font-semibold text-white">
                        Blacklist Customer
                    </h3>
                    <p className="text-sm text-zinc-400 mt-1">
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

                        <label className="block text-sm font-medium text-zinc-300 mb-2">
                            Reason for blacklisting <span className="text-red-400">*</span>
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
                            className="w-full px-3 py-2 bg-zinc-800 border border-zinc-700 rounded-lg text-white placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                        />
                        <div className="flex justify-between mt-1">
                            {error ? (
                                <p className="text-sm text-red-400">{error}</p>
                            ) : (
                                <span />
                            )}
                            <p className="text-xs text-zinc-500">{reason.length}/500</p>
                        </div>

                        {/* No-show count info */}
                        {customer.noShowCount > 0 && (
                            <p className="text-sm text-zinc-400 mt-3">
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
                    <div className="px-6 py-4 border-t border-zinc-800 flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 text-sm font-medium text-zinc-400 hover:text-white transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 text-sm font-medium bg-red-600 text-white rounded-lg hover:bg-red-500 transition-colors"
                        >
                            Blacklist Customer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
