import React from "react";
import { getBookingStatusStyles, getPaymentStatusStyles } from "../../utils/statusUtils";

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
  const getStyles = () => {
    if (type === "booking") {
      return getBookingStatusStyles(status);
    } else {
      return getPaymentStatusStyles(status);
    }
  };

  return (
    <span
      className={`px-2 py-1 rounded text-xs font-semibold ${getStyles()} ${className}`}
    >
      {status}
    </span>
  );
};

export default StatusBadge;
