import { useState } from "react";
import type { Service } from "../../../types";

interface ServiceCardProps {
  service: Service;
  onEdit: (service: Service) => void;
  onDeactivate: (id: number) => void;
  isInactive?: boolean;
}

export default function ServiceCard({
  service,
  onEdit,
  onDeactivate,
  isInactive = false,
}: ServiceCardProps) {
  const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);

  const handleDeactivate = () => {
    onDeactivate(service.id);
    setShowDeactivateConfirm(false);
  };

  return (
    <div className={`p-4 rounded-lg shadow border ${isInactive
      ? "bg-[var(--bg-surface)]/50 border-[var(--border-subtle)]/50"
      : "bg-[var(--bg-surface)] border-[var(--border-subtle)]"
      }`}>
      <div className="flex justify-between items-start mb-2">
        <div className="flex items-center gap-2">
          <h3 className={`text-lg font-bold ${isInactive ? "text-[var(--text-subtle)]" : "text-[var(--text-primary)]"}`}>
            {service.name}
          </h3>
          {isInactive && (
            <span className="text-xs bg-[var(--bg-muted)] text-[var(--text-muted)] px-2 py-0.5 rounded">
              Inactive
            </span>
          )}
        </div>
        {service.categoryName && (
          <span className={`text-xs px-2 py-1 rounded-full ${isInactive
            ? "bg-[var(--bg-muted)]/30 text-[var(--text-subtle)]"
            : "bg-[var(--accent)]/30 text-[var(--accent-text)]"
            }`}>
            {service.categoryName}
          </span>
        )}
      </div>
      <p className={`mb-3 text-sm ${isInactive ? "text-[var(--text-faint)]" : "text-[var(--text-muted)]"}`}>
        {service.description}
      </p>
      <div className={`space-y-1 text-sm ${isInactive ? "text-[var(--text-faint)]" : "text-[var(--text-subtle)]"}`}>
        <div className="flex justify-between">
          <span>Duration:</span>
          <span>{service.durationMinutes} min</span>
        </div>
        <div className="flex justify-between">
          <span>Price:</span>
          <span className={`font-semibold ${isInactive ? "text-[var(--text-subtle)]" : "text-[var(--text-secondary)]"}`}>
            €{service.price.toFixed(2)}
          </span>
        </div>
        <div className="flex justify-between">
          <span>Deposit:</span>
          <span>{service.depositPercentage}%</span>
        </div>
      </div>

      {/* Action buttons */}
      <div className="mt-4 pt-3 border-t border-[var(--border-subtle)] flex justify-end gap-2">
        <button
          onClick={() => onEdit(service)}
          className="text-[var(--accent-text)] hover:text-[var(--accent-text)] text-sm font-medium"
        >
          Edit
        </button>
        {!isInactive && (
          !showDeactivateConfirm ? (
            <button
              onClick={() => setShowDeactivateConfirm(true)}
              className="text-amber-400 hover:text-amber-300 text-sm font-medium"
            >
              Deactivate
            </button>
          ) : (
            <div className="flex gap-1">
              <button
                onClick={handleDeactivate}
                className="text-amber-400 hover:text-amber-300 text-sm font-medium"
              >
                Confirm
              </button>
              <button
                onClick={() => setShowDeactivateConfirm(false)}
                className="text-[var(--text-muted)] hover:text-[var(--text-secondary)] text-sm font-medium"
              >
                Cancel
              </button>
            </div>
          )
        )}
      </div>
    </div>
  );
}
