import React from "react";
import { getBookingStatusStyles, getPaymentStatusStyles, type StatusStyle } from "../../utils/statusUtils";

interface StatusBadgeProps {
  status: string;
  type: "booking" | "payment";
  className?: string;
}

/**
 * Reusable status badge component for booking and payment statuses
 * Provides consistent styling across the application
 */
export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  type,
  className = "",
}) => {
  const getStyles = (): StatusStyle => {
    if (type === "booking") {
      return getBookingStatusStyles(status);
    } else {
      return getPaymentStatusStyles(status);
    }
  };

  const styles = getStyles();

  return (
    <span
      className={`px-2 py-1 rounded text-xs font-semibold border ${className}`}
      style={{
        backgroundColor: styles.backgroundColor,
        color: styles.color,
        borderColor: styles.borderColor,
      }}
    >
      {status}
    </span>
  );
};

export default StatusBadge;
