/**
 * Utility functions for status formatting and styling
 */

/**
 * Get Tailwind CSS classes for booking status badges
 */
export const getBookingStatusStyles = (status: string): string => {
  switch (status) {
    case "COMPLETED":
      return "bg-green-100 text-green-800";
    case "CONFIRMED":
      return "bg-blue-100 text-blue-800";
    case "PENDING":
      return "bg-yellow-100 text-yellow-800";
    case "CANCELLED":
      return "bg-red-100 text-red-800";
    case "NO_SHOW":
      return "bg-gray-100 text-gray-800";
    default:
      return "bg-gray-100 text-gray-800";
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
  if (loading) return "bg-gray-400";

  if (status === "COMPLETED") return "bg-green-600";
  if (status === "NO_SHOW") return "bg-gray-600";
  if (status === "CANCELLED") return "bg-red-400";

  // Default active button colors
  if (actionType === "complete") return "bg-blue-600 hover:bg-blue-700";
  if (actionType === "no-show") return "bg-orange-600 hover:bg-orange-700";

  return "bg-gray-400";
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
      return "bg-green-100 text-green-800";
    case "DEPOSIT_PAID":
      return "bg-blue-100 text-blue-800";
    case "PENDING":
    case "DEPOSIT_PENDING":
      return "bg-yellow-100 text-yellow-800";
    case "REFUNDED":
      return "bg-purple-100 text-purple-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
};
