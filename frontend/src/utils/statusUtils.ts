/**
 * Utility functions for status formatting and styling
 */

export interface StatusStyle {
  backgroundColor: string;
  color: string;
  borderColor: string;
}

/**
 * Get CSS variable-based styles for booking status badges
 */
export const getBookingStatusStyles = (status: string): StatusStyle => {
  switch (status) {
    case "COMPLETED":
      return {
        backgroundColor: "var(--success-muted)",
        color: "var(--success-text-light)",
        borderColor: "var(--success-border)",
      };
    case "CONFIRMED":
      return {
        backgroundColor: "var(--accent-muted)",
        color: "var(--accent-text-light)",
        borderColor: "var(--accent-muted)",
      };
    case "PENDING":
      return {
        backgroundColor: "var(--warning-muted)",
        color: "var(--warning-text-light)",
        borderColor: "var(--warning-border)",
      };
    case "CANCELLED":
      return {
        backgroundColor: "var(--danger-muted)",
        color: "var(--danger-text-light)",
        borderColor: "var(--danger-border)",
      };
    case "NO_SHOW":
      return {
        backgroundColor: "var(--bg-elevated)",
        color: "var(--text-secondary)",
        borderColor: "var(--border-default)",
      };
    default:
      return {
        backgroundColor: "var(--bg-elevated)",
        color: "var(--text-secondary)",
        borderColor: "var(--border-default)",
      };
  }
};

/**
 * Get Tailwind CSS classes for button colors based on booking status
 */
export const getBookingActionButtonStyles = (
  status: string,
  loading: boolean,
  actionType: "complete" | "no-show",
): string => {
  if (loading) return "bg-[var(--bg-muted)]";

  if (status === "COMPLETED") return "bg-[var(--success)]";
  if (status === "NO_SHOW") return "bg-[var(--bg-subtle)]";
  if (status === "CANCELLED") return "bg-[var(--danger-hover)]";

  // Default active button colors
  if (actionType === "complete")
    return "bg-[var(--accent)] hover:bg-[var(--accent-hover)]";
  if (actionType === "no-show")
    return "bg-[var(--orange)] hover:bg-[var(--orange-hover)]";

  return "bg-[var(--bg-muted)]";
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
 * Get CSS variable-based styles for payment status badges
 */
export const getPaymentStatusStyles = (status: string): StatusStyle => {
  switch (status) {
    case "FULLY_PAID":
      return {
        backgroundColor: "var(--success-muted)",
        color: "var(--success-text-light)",
        borderColor: "var(--success-border)",
      };
    case "DEPOSIT_PAID":
      return {
        backgroundColor: "var(--accent-muted)",
        color: "var(--accent-text-light)",
        borderColor: "var(--accent-muted)",
      };
    case "PENDING":
    case "DEPOSIT_PENDING":
      return {
        backgroundColor: "var(--warning-muted)",
        color: "var(--warning-text-light)",
        borderColor: "var(--warning-border)",
      };
    case "REFUNDED":
      return {
        backgroundColor: "var(--purple-muted)",
        color: "var(--purple-text-light)",
        borderColor: "var(--purple-border-dark)",
      };
    default:
      return {
        backgroundColor: "var(--bg-elevated)",
        color: "var(--text-secondary)",
        borderColor: "var(--border-default)",
      };
  }
};
