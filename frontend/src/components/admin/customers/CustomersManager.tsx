import { useEffect, useState } from "react";
import { customersApi } from "../../../api/endpoints";
import type { Customer } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import CustomerRow from "./CustomerRow";
import BlacklistModal from "./BlacklistModal";

export default function CustomersManager() {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [loading, setLoading] = useState(true);
    const [status, setStatus] = useState<{
        type: "success" | "error";
        message: string;
    } | null>(null);
    const [blacklistTarget, setBlacklistTarget] = useState<Customer | null>(null);
    const [filterBlacklisted, setFilterBlacklisted] = useState<
        "all" | "active" | "blacklisted"
    >("all");
    const [searchTerm, setSearchTerm] = useState("");

    useEffect(() => {
        fetchCustomers();
    }, []);

    const fetchCustomers = async () => {
        try {
            setLoading(true);
            const response = await customersApi.getAll();
            setCustomers(response.data.customers);
            setStatus(null);
        } catch {
            setStatus({ type: "error", message: "Failed to load customers" });
        } finally {
            setLoading(false);
        }
    };

    const handleBlacklist = async (reason: string) => {
        if (!blacklistTarget) return;

        try {
            await customersApi.blacklist(blacklistTarget.id, { reason });
            setStatus({
                type: "success",
                message: `${blacklistTarget.firstName} ${blacklistTarget.lastName} has been blacklisted`,
            });
            setBlacklistTarget(null);
            fetchCustomers();
        } catch {
            setStatus({ type: "error", message: "Failed to blacklist customer" });
        }
    };

    const handleUnblacklist = async (customer: Customer) => {
        try {
            await customersApi.unblacklist(customer.id);
            setStatus({
                type: "success",
                message: `${customer.firstName} ${customer.lastName} has been removed from blacklist`,
            });
            fetchCustomers();
        } catch {
            setStatus({
                type: "error",
                message: "Failed to remove customer from blacklist",
            });
        }
    };

    // Filter and search customers
    const filteredCustomers = customers.filter((customer) => {
        // Filter by blacklist status
        if (filterBlacklisted === "active" && customer.blacklisted) return false;
        if (filterBlacklisted === "blacklisted" && !customer.blacklisted)
            return false;

        // Search filter
        if (searchTerm) {
            const search = searchTerm.toLowerCase();
            return (
                customer.firstName.toLowerCase().includes(search) ||
                customer.lastName.toLowerCase().includes(search) ||
                customer.email.toLowerCase().includes(search) ||
                customer.phone.includes(search)
            );
        }

        return true;
    });

    if (loading) {
        return (
            <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
            </div>
        );
    }

    return (
        <div>
            <div className="mb-6">
                <h2 className="text-2xl font-bold text-white mb-4">Customers</h2>

                {/* Filters */}
                <div className="flex flex-col sm:flex-row gap-4">
                    <input
                        type="text"
                        placeholder="Search by name, email, or phone..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="flex-1 px-4 py-2 bg-zinc-900 border border-zinc-700 rounded-lg text-white placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <select
                        value={filterBlacklisted}
                        onChange={(e) =>
                            setFilterBlacklisted(
                                e.target.value as "all" | "active" | "blacklisted"
                            )
                        }
                        className="px-4 py-2 bg-zinc-900 border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                        <option value="all">All Customers</option>
                        <option value="active">Active Only</option>
                        <option value="blacklisted">Blacklisted Only</option>
                    </select>
                </div>
            </div>

            {status && <StatusMessage type={status.type} message={status.message} />}

            {filteredCustomers.length === 0 ? (
                <EmptyState
                    message={
                        searchTerm || filterBlacklisted !== "all"
                            ? "No customers match your filters."
                            : "No customers yet."
                    }
                />
            ) : (
                <div className="bg-zinc-900 rounded-lg border border-zinc-800 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-zinc-800">
                                    <th className="px-4 py-3 text-left text-sm font-semibold text-zinc-400">
                                        Customer
                                    </th>
                                    <th className="px-4 py-3 text-left text-sm font-semibold text-zinc-400">
                                        Contact
                                    </th>
                                    <th className="px-4 py-3 text-center text-sm font-semibold text-zinc-400">
                                        Status
                                    </th>
                                    <th className="px-4 py-3 text-right text-sm font-semibold text-zinc-400">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-zinc-800">
                                {filteredCustomers.map((customer) => (
                                    <CustomerRow
                                        key={customer.id}
                                        customer={customer}
                                        onBlacklist={() => setBlacklistTarget(customer)}
                                        onUnblacklist={() => handleUnblacklist(customer)}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Blacklist Modal */}
            {blacklistTarget && (
                <BlacklistModal
                    customer={blacklistTarget}
                    onConfirm={handleBlacklist}
                    onCancel={() => setBlacklistTarget(null)}
                />
            )}
        </div>
    );
}
