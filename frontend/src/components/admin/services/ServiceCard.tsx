import { useState } from "react";
import type { Service } from "../../../types";

interface ServiceCardProps {
  service: Service;
  onEdit: (service: Service) => void;
  onDeactivate: (id: number) => void;
}

export default function ServiceCard({
  service,
  onEdit,
  onDeactivate,
}: ServiceCardProps) {
  const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);

  const handleDeactivate = () => {
    onDeactivate(service.id);
    setShowDeactivateConfirm(false);
  };

  return (
    <div className="p-4 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-lg font-bold text-white">{service.name}</h3>
        {service.categoryName && (
          <span className="text-xs bg-indigo-600/30 text-indigo-300 px-2 py-1 rounded-full">
            {service.categoryName}
          </span>
        )}
      </div>
      <p className="text-zinc-400 mb-3 text-sm">{service.description}</p>
      <div className="space-y-1 text-sm text-zinc-500">
        <div className="flex justify-between">
          <span>Duration:</span>
          <span>{service.durationMinutes} min</span>
        </div>
        <div className="flex justify-between">
          <span>Price:</span>
          <span className="font-semibold text-zinc-300">
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
        {!showDeactivateConfirm ? (
          <button
            onClick={() => setShowDeactivateConfirm(true)}
            className="text-red-400 hover:text-red-300 text-sm font-medium"
          >
            Deactivate
          </button>
        ) : (
          <div className="flex gap-1">
            <button
              onClick={handleDeactivate}
              className="text-red-400 hover:text-red-300 text-sm font-medium"
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
        )}
      </div>
    </div>
  );
}
