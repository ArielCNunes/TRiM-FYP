import { useState } from "react";
import type { Barber } from "../../../types";

interface BarberCardProps {
  barber: Barber;
  onEdit: (barber: Barber) => void;
  onDeactivate: (id: number) => void;
  onManageAvailability?: (barber: Barber) => void;
  isInactive?: boolean;
}

export default function BarberCard({
  barber,
  onEdit,
  onDeactivate,
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
        ? "bg-zinc-900/50 border-zinc-800/50"
        : "bg-zinc-900 border-zinc-800"
        }`}
    >
      <div className="flex items-start gap-3">
        {/* Profile Image */}
        {barber.profileImageUrl ? (
          <img
            src={barber.profileImageUrl}
            alt={`${barber.user.firstName} ${barber.user.lastName}`}
            className={`w-12 h-12 rounded-full object-cover border-2 ${isInactive ? "border-zinc-700" : "border-indigo-500"
              }`}
          />
        ) : (
          <div
            className={`w-12 h-12 rounded-full flex items-center justify-center text-lg font-bold ${isInactive
              ? "bg-zinc-700 text-zinc-500"
              : "bg-indigo-600 text-white"
              }`}
          >
            {barber.user.firstName.charAt(0)}
            {barber.user.lastName.charAt(0)}
          </div>
        )}

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h3
              className={`text-lg font-bold truncate ${isInactive ? "text-zinc-500" : "text-white"
                }`}
            >
              {barber.user.firstName} {barber.user.lastName}
            </h3>
            {isInactive && (
              <span className="text-xs bg-zinc-700 text-zinc-400 px-2 py-0.5 rounded">
                Inactive
              </span>
            )}
          </div>
          <p
            className={`text-sm truncate ${isInactive ? "text-zinc-600" : "text-zinc-400"
              }`}
          >
            {barber.user.email}
          </p>
        </div>
      </div>

      {barber.bio && (
        <p
          className={`text-sm mt-3 ${isInactive ? "text-zinc-600" : "text-zinc-300"
            }`}
        >
          {barber.bio}
        </p>
      )}

      <p className={`text-xs mt-2 ${isInactive ? "text-zinc-600" : "text-zinc-500"}`}>
        {barber.user.phone}
      </p>

      {/* Action buttons */}
      <div className="mt-4 pt-3 border-t border-zinc-800 flex justify-between">
        <div>
          {onManageAvailability && !isInactive && (
            <button
              onClick={() => onManageAvailability(barber)}
              className="text-emerald-400 hover:text-emerald-300 text-sm font-medium"
            >
              Availability
            </button>
          )}
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(barber)}
            className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
          >
            Edit
          </button>
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
                  className="text-zinc-400 hover:text-zinc-300 text-sm font-medium"
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
