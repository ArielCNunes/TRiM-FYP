/**
 * Utility functions for status formatting and styling
 */

/**
 * Get Tailwind CSS classes for booking status badges
 */
export const getBookingStatusStyles = (status: string): string => {
  switch (status) {
    case "COMPLETED":
      return "bg-emerald-900/30 text-emerald-300 border border-emerald-800";
    case "CONFIRMED":
      return "bg-indigo-900/30 text-indigo-300 border border-indigo-800";
    case "PENDING":
      return "bg-yellow-900/30 text-yellow-300 border border-yellow-800";
    case "CANCELLED":
      return "bg-red-900/30 text-red-300 border border-red-800";
    case "NO_SHOW":
      return "bg-zinc-800 text-zinc-300 border border-zinc-700";
    default:
      return "bg-zinc-800 text-zinc-300 border border-zinc-700";
  }
};

/**
 * Get Tailwind CSS classes for button colors based on booking status
 */
export const getBookingActionButtonStyles = (
  status: string,
  loading: boolean,
  actionType: "complete" | "no-show"
): string => {
  if (loading) return "bg-zinc-700";

  if (status === "COMPLETED") return "bg-emerald-600";
  if (status === "NO_SHOW") return "bg-zinc-600";
  if (status === "CANCELLED") return "bg-red-500";

  // Default active button colors
  if (actionType === "complete") return "bg-indigo-600 hover:bg-indigo-500";
  if (actionType === "no-show") return "bg-orange-600 hover:bg-orange-500";

  return "bg-zinc-700";
};

/**
 * Format payment status for display
 */
export const formatPaymentStatus = (status: string): string => {
  switch (status) {
    case "DEPOSIT_PAID":
      return "Deposit Paid";
    case "FULLY_PAID":
      return "Fully Paid";
    case "PENDING":
      return "Pending Payment";
    case "DEPOSIT_PENDING":
      return "Deposit Pending";
    case "REFUNDED":
      return "Refunded";
    default:
      return status;
  }
};

/**
 * Get Tailwind CSS classes for payment status badges
 */
export const getPaymentStatusStyles = (status: string): string => {
  switch (status) {
    case "FULLY_PAID":
      return "bg-emerald-900/30 text-emerald-300 border border-emerald-800";
    case "DEPOSIT_PAID":
      return "bg-indigo-900/30 text-indigo-300 border border-indigo-800";
    case "PENDING":
    case "DEPOSIT_PENDING":
      return "bg-yellow-900/30 text-yellow-300 border border-yellow-800";
    case "REFUNDED":
      return "bg-purple-900/30 text-purple-300 border border-purple-800";
    default:
      return "bg-zinc-800 text-zinc-300 border border-zinc-700";
  }
};
