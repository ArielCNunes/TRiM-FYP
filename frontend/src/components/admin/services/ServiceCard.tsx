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
        ? "bg-zinc-900/50 border-zinc-800/50"
        : "bg-zinc-900 border-zinc-800"
      }`}>
      <div className="flex justify-between items-start mb-2">
        <div className="flex items-center gap-2">
          <h3 className={`text-lg font-bold ${isInactive ? "text-zinc-500" : "text-white"}`}>
            {service.name}
          </h3>
          {isInactive && (
            <span className="text-xs bg-zinc-700 text-zinc-400 px-2 py-0.5 rounded">
              Inactive
            </span>
          )}
        </div>
        {service.categoryName && (
          <span className={`text-xs px-2 py-1 rounded-full ${isInactive
              ? "bg-zinc-700/30 text-zinc-500"
              : "bg-indigo-600/30 text-indigo-300"
            }`}>
            {service.categoryName}
          </span>
        )}
      </div>
      <p className={`mb-3 text-sm ${isInactive ? "text-zinc-600" : "text-zinc-400"}`}>
        {service.description}
      </p>
      <div className={`space-y-1 text-sm ${isInactive ? "text-zinc-600" : "text-zinc-500"}`}>
        <div className="flex justify-between">
          <span>Duration:</span>
          <span>{service.durationMinutes} min</span>
        </div>
        <div className="flex justify-between">
          <span>Price:</span>
          <span className={`font-semibold ${isInactive ? "text-zinc-500" : "text-zinc-300"}`}>
            €{service.price.toFixed(2)}
          </span>
        </div>
        <div className="flex justify-between">
          <span>Deposit:</span>
          <span>{service.depositPercentage}%</span>
        </div>
      </div>

      {/* Action buttons */}
      <div className="mt-4 pt-3 border-t border-zinc-800 flex justify-end gap-2">
        <button
          onClick={() => onEdit(service)}
          className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
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
                className="text-zinc-400 hover:text-zinc-300 text-sm font-medium"
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
