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
        <tr className="hover:bg-zinc-800/50 transition-colors">
            {/* Customer Name & Join Date */}
            <td className="px-4 py-3">
                <div>
                    <p className="font-medium text-white">
                        {customer.firstName} {customer.lastName}
                    </p>
                    <p className="text-sm text-zinc-500">
                        Joined {formatDate(customer.createdAt)}
                    </p>
                </div>
            </td>

            {/* Contact Info */}
            <td className="px-4 py-3">
                <div>
                    <p className="text-sm text-zinc-300">{customer.email}</p>
                    <p className="text-sm text-zinc-500">{customer.phone}</p>
                </div>
            </td>

            {/* Status */}
            <td className="px-4 py-3 text-center">
                {customer.blacklisted ? (
                    <div className="inline-flex flex-col items-center">
                        <span className="px-2 py-1 text-xs font-medium bg-red-500/20 text-red-400 rounded-full">
                            Blacklisted
                        </span>
                        {customer.blacklistReason && (
                            <span
                                className="text-xs text-zinc-500 mt-1 max-w-[150px] truncate"
                                title={customer.blacklistReason}
                            >
                                {customer.blacklistReason}
                            </span>
                        )}
                    </div>
                ) : (
                    <span className="px-2 py-1 text-xs font-medium bg-emerald-500/20 text-emerald-400 rounded-full">
                        Active
                    </span>
                )}
            </td>

            {/* Actions */}
            <td className="px-4 py-3 text-right">
                {customer.blacklisted ? (
                    <button
                        onClick={onUnblacklist}
                        className="px-3 py-1.5 text-sm font-medium text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 rounded-md transition-colors"
                    >
                        Remove Blacklist
                    </button>
                ) : (
                    <button
                        onClick={onBlacklist}
                        className="px-3 py-1.5 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-md transition-colors"
                    >
                        Blacklist
                    </button>
                )}
            </td>
        </tr>
    );
}
