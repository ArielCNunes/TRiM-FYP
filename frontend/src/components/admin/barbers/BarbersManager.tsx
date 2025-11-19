import { useEffect, useState } from "react";
import { barbersApi } from "../../../api/endpoints";
import type { Barber } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import BarberForm from "./BarberForm";
import BarberCard from "./BarberCard";

export default function BarbersManager() {
  const [barbers, setBarbers] = useState<Barber[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  useEffect(() => {
    fetchBarbers();
  }, []);

  const fetchBarbers = async () => {
    try {
      const response = await barbersApi.getActive();
      setBarbers(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load barbers" });
    }
  };

  const handleSuccess = () => {
    setShowForm(false);
    setStatus({ type: "success", message: "Barber created successfully" });
    fetchBarbers();
  };

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Barbers</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-500 transition shadow-lg shadow-indigo-500/20"
        >
          {showForm ? "Cancel" : "Add Barber"}
        </button>
      </div>

      {showForm && (
        <BarberForm
          onSuccess={handleSuccess}
          onCancel={() => setShowForm(false)}
        />
      )}

      {status && <StatusMessage type={status.type} message={status.message} />}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {barbers.map((barber) => (
          <BarberCard key={barber.id} barber={barber} />
        ))}
      </div>

      {barbers.length === 0 && (
        <EmptyState message="No barbers yet. Create one to get started." />
      )}
    </div>
  );
}
