import type { Customer } from "../../../types";

interface CustomerRowProps {
    customer: Customer;
    onBlacklist: () => void;
    onUnblacklist: () => void;
}

export default function CustomerRow({
    customer,
    onBlacklist,
    onUnblacklist,
}: CustomerRowProps) {
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("en-IE", {
            day: "numeric",
            month: "short",
            year: "numeric",
        });
    };

    return (
        <tr className="hover:bg-[var(--bg-elevated)]/50 transition-colors">
            {/* Customer Name & Join Date */}
            <td className="px-4 py-3">
                <div>
                    <p className="font-medium text-[var(--text-primary)]">
                        {customer.firstName} {customer.lastName}
                    </p>
                    <p className="text-sm text-[var(--text-subtle)]">
                        Joined {formatDate(customer.createdAt)}
                    </p>
                </div>
            </td>

            {/* Contact Info */}
            <td className="px-4 py-3">
                <div>
                    <p className="text-sm text-[var(--text-secondary)]">{customer.email}</p>
                    <p className="text-sm text-[var(--text-subtle)]">{customer.phone}</p>
                </div>
            </td>

            {/* Status */}
            <td className="px-4 py-3 text-center">
                {customer.blacklisted ? (
                    <div className="inline-flex flex-col items-center">
                        <span className="px-2 py-1 text-xs font-medium bg-red-500/20 text-[var(--danger-text)] rounded-full">
                            Blacklisted
                        </span>
                        {customer.blacklistReason && (
                            <span
                                className="text-xs text-[var(--text-subtle)] mt-1 max-w-[150px] truncate"
                                title={customer.blacklistReason}
                            >
                                {customer.blacklistReason}
                            </span>
                        )}
                    </div>
                ) : (
                    <span className="px-2 py-1 text-xs font-medium bg-emerald-500/20 text-[var(--success-text)] rounded-full">
                        Active
                    </span>
                )}
            </td>

            {/* Actions */}
            <td className="px-4 py-3 text-right">
                {customer.blacklisted ? (
                    <button
                        onClick={onUnblacklist}
                        className="px-3 py-1.5 text-sm font-medium text-[var(--success-text)] hover:text-[var(--success-text-light)] hover:bg-emerald-500/10 rounded-md transition-colors"
                    >
                        Remove Blacklist
                    </button>
                ) : (
                    <button
                        onClick={onBlacklist}
                        className="px-3 py-1.5 text-sm font-medium text-[var(--danger-text)] hover:text-[var(--danger-text-light)] hover:bg-red-500/10 rounded-md transition-colors"
                    >
                        Blacklist
                    </button>
                )}
            </td>
        </tr>
    );
}
