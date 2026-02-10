import { useState } from "react";
import type { Barber } from "../../../types";

interface BarberCardProps {
  barber: Barber;
  onEdit: (barber: Barber) => void;
  onDeactivate: (id: number) => void;
  onReactivate?: (id: number) => void;
  onManageAvailability?: (barber: Barber) => void;
  isInactive?: boolean;
}

export default function BarberCard({
  barber,
  onEdit,
  onDeactivate,
  onReactivate,
  onManageAvailability,
  isInactive = false,
}: BarberCardProps) {
  const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);

  const handleDeactivate = () => {
    onDeactivate(barber.id);
    setShowDeactivateConfirm(false);
  };

  return (
    <div
      className={`p-4 rounded-lg shadow border ${isInactive
        ? "bg-[var(--bg-surface)]/50 border-[var(--border-subtle)]/50"
        : "bg-[var(--bg-surface)] border-[var(--border-subtle)]"
        }`}
    >
      <div className="flex items-start gap-3">
        {/* Profile Image */}
        {barber.profileImageUrl ? (
          <img
            src={barber.profileImageUrl}
            alt={`${barber.user.firstName} ${barber.user.lastName}`}
            className="w-12 h-12 rounded-full object-cover"
          />
        ) : (
          <div
            className={`w-12 h-12 rounded-full flex items-center justify-center text-lg font-bold ${isInactive
              ? "bg-[var(--bg-muted)] text-[var(--text-subtle)]"
              : "bg-[var(--accent)] text-[var(--text-primary)]"
              }`}
          >
            {barber.user.firstName.charAt(0)}
            {barber.user.lastName.charAt(0)}
          </div>
        )}

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h3
              className={`text-lg font-bold truncate ${isInactive ? "text-[var(--text-subtle)]" : "text-[var(--text-primary)]"
                }`}
            >
              {barber.user.firstName} {barber.user.lastName}
            </h3>
            {isInactive && (
              <span className="text-xs bg-[var(--bg-muted)] text-[var(--text-muted)] px-2 py-0.5 rounded">
                Inactive
              </span>
            )}
          </div>
          <p
            className={`text-sm truncate ${isInactive ? "text-zinc-600" : "text-[var(--text-muted)]"
              }`}
          >
            {barber.user.email}
          </p>
        </div>
      </div>

      {barber.bio && (
        <p
          className={`text-sm mt-3 ${isInactive ? "text-zinc-600" : "text-[var(--text-secondary)]"
            }`}
        >
          {barber.bio}
        </p>
      )}

      <p className={`text-xs mt-2 ${isInactive ? "text-zinc-600" : "text-[var(--text-subtle)]"}`}>
        {barber.user.phone}
      </p>

      {/* Action buttons */}
      <div className="mt-4 pt-3 border-t border-[var(--border-subtle)] flex justify-between">
        <div>
          {onManageAvailability && !isInactive && (
            <button
              onClick={() => onManageAvailability(barber)}
              className="text-[var(--success-text)] hover:text-emerald-300 text-sm font-medium"
            >
              Availability
            </button>
          )}
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(barber)}
            className="text-[var(--accent-text)] hover:text-indigo-300 text-sm font-medium"
          >
            Edit
          </button>
          {isInactive && onReactivate && (
            <button
              onClick={() => onReactivate(barber.id)}
              className="text-[var(--success-text)] hover:text-emerald-300 text-sm font-medium"
            >
              Reactivate
            </button>
          )}
          {!isInactive &&
            (!showDeactivateConfirm ? (
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
            ))}
        </div>
      </div>
    </div>
  );
}
