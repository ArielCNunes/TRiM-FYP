import { useEffect, useState } from "react";
import { barbersApi } from "../../../api/endpoints";
import type { Barber } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import BarberForm from "./BarberForm";
import BarberCard from "./BarberCard";
import BarberAvailabilityManager from "../../barber/BarberAvailabilityManager";

export default function BarbersManager() {
  const [activeBarbers, setActiveBarbers] = useState<Barber[]>([]);
  const [inactiveBarbers, setInactiveBarbers] = useState<Barber[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingBarber, setEditingBarber] = useState<Barber | null>(null);
  const [availabilityBarber, setAvailabilityBarber] = useState<Barber | null>(null);
  const [showInactive, setShowInactive] = useState(false);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  useEffect(() => {
    fetchBarbers();
  }, []);

  const fetchBarbers = async () => {
    try {
      setLoading(true);
      const response = await barbersApi.getAll();
      const allBarbers = response.data;

      // Separate active and inactive barbers
      setActiveBarbers(allBarbers.filter((b) => b.active !== false));
      setInactiveBarbers(allBarbers.filter((b) => b.active === false));
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load barbers" });
    } finally {
      setLoading(false);
    }
  };

  const handleSuccess = () => {
    setShowForm(false);
    setStatus({
      type: "success",
      message: editingBarber
        ? "Barber updated successfully"
        : "Barber created successfully",
    });
    setEditingBarber(null);
    fetchBarbers();
  };

  const handleEdit = (barber: Barber) => {
    setEditingBarber(barber);
    setShowForm(true);
  };

  const handleDeactivate = async (id: number) => {
    try {
      await barbersApi.deactivate(id);
      setStatus({ type: "success", message: "Barber deactivated successfully" });
      fetchBarbers();
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to deactivate barber";
      setStatus({ type: "error", message });
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingBarber(null);
  };

  const handleManageAvailability = (barber: Barber) => {
    setAvailabilityBarber(barber);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Barbers</h2>
        <button
          onClick={() => {
            setEditingBarber(null);
            setShowForm(!showForm);
          }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-500 transition shadow-lg shadow-indigo-500/20"
        >
          {showForm && !editingBarber ? "Cancel" : "Add Barber"}
        </button>
      </div>

      {showForm && (
        <BarberForm
          editingBarber={editingBarber}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      )}

      {status && <StatusMessage type={status.type} message={status.message} />}

      {activeBarbers.length === 0 && inactiveBarbers.length === 0 ? (
        <EmptyState message="No barbers yet. Create one to get started." />
      ) : (
        <>
          {/* Active Barbers */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {activeBarbers.map((barber) => (
              <BarberCard
                key={barber.id}
                barber={barber}
                onEdit={handleEdit}
                onDeactivate={handleDeactivate}
                onManageAvailability={handleManageAvailability}
              />
            ))}
          </div>

          {activeBarbers.length === 0 && (
            <EmptyState message="No active barbers. Check inactive barbers below or create a new one." />
          )}

          {/* Inactive Barbers Dropdown */}
          {inactiveBarbers.length > 0 && (
            <div className="mt-8 border border-zinc-800 rounded-lg overflow-hidden">
              <button
                onClick={() => setShowInactive(!showInactive)}
                className="w-full px-4 py-3 bg-zinc-900 flex items-center justify-between text-left hover:bg-zinc-800/50 transition-colors"
              >
                <div className="flex items-center gap-2">
                  <span className="text-zinc-400 font-medium">Inactive Barbers</span>
                  <span className="text-xs bg-zinc-700 text-zinc-400 px-2 py-0.5 rounded-full">
                    {inactiveBarbers.length}
                  </span>
                </div>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className={`h-5 w-5 text-zinc-500 transition-transform duration-200 ${showInactive ? "rotate-180" : ""
                    }`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>

              {showInactive && (
                <div className="p-4 bg-zinc-900/50 border-t border-zinc-800">
                  <p className="text-xs text-zinc-500 mb-4">
                    These barbers are hidden from the booking flow. Contact support to reactivate a barber.
                  </p>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {inactiveBarbers.map((barber) => (
                      <BarberCard
                        key={barber.id}
                        barber={barber}
                        onEdit={handleEdit}
                        onDeactivate={handleDeactivate}
                        isInactive
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}

      {/* Availability Modal */}
      {availabilityBarber && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-zinc-900 rounded-lg shadow-xl border border-zinc-800 w-full max-w-2xl max-h-[90vh] overflow-hidden">
            {/* Modal Header */}
            <div className="flex items-center justify-between p-4 border-b border-zinc-800">
              <div>
                <h3 className="text-xl font-bold text-white">
                  Manage Availability
                </h3>
                <p className="text-sm text-zinc-400">
                  {availabilityBarber.user.firstName} {availabilityBarber.user.lastName}
                </p>
              </div>
              <button
                onClick={() => setAvailabilityBarber(null)}
                className="text-zinc-400 hover:text-white transition p-1"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-4 overflow-y-auto max-h-[calc(90vh-80px)]">
              <BarberAvailabilityManager barberId={availabilityBarber.id} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
