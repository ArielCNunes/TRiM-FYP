import { useState } from "react";
import { servicesApi } from "../../../api/endpoints";

interface ServiceFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export default function ServiceForm({ onSuccess, onCancel }: ServiceFormProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    durationMinutes: 30,
    price: 0,
    depositPercentage: 50,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await servicesApi.create({
        name: formData.name,
        description: formData.description,
        durationMinutes: parseInt(String(formData.durationMinutes)),
        price: parseFloat(String(formData.price)),
        depositPercentage: parseInt(String(formData.depositPercentage)),
        active: true,
      } as any);

      setFormData({
        name: "",
        description: "",
        durationMinutes: 30,
        price: 0,
        depositPercentage: 50,
      });
      onSuccess();
    } catch (error: any) {
      const message = error.response?.data?.message || "Failed to create service";
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mb-8 p-6 bg-white rounded-lg shadow">
      <h3 className="text-xl font-bold mb-4">Create New Service</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">
            Service Name
          </label>
          <input
            type="text"
            required
            className="w-full border rounded-md p-2"
            value={formData.name}
            onChange={(e) =>
              setFormData({ ...formData, name: e.target.value })
            }
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">
            Description
          </label>
          <textarea
            required
            className="w-full border rounded-md p-2"
            rows={3}
            value={formData.description}
            onChange={(e) =>
              setFormData({ ...formData, description: e.target.value })
            }
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">
              Duration (minutes)
            </label>
            <input
              type="number"
              required
              min="15"
              step="15"
              className="w-full border rounded-md p-2"
              value={formData.durationMinutes}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  durationMinutes: parseInt(e.target.value),
                })
              }
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">
              Price (€)
            </label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              className="w-full border rounded-md p-2"
              value={formData.price}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  price: parseFloat(e.target.value),
                })
              }
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">
            Deposit Percentage (%)
          </label>
          <input
            type="number"
            required
            min="0"
            max="100"
            step="1"
            className="w-full border rounded-md p-2"
            value={formData.depositPercentage}
            onChange={(e) =>
              setFormData({
                ...formData,
                depositPercentage: parseInt(e.target.value),
              })
            }
          />
          <p className="text-xs text-gray-500 mt-1">
            Percentage of the total price required as deposit (0-100%)
          </p>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {loading ? "Creating..." : "Create Service"}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="px-6 bg-gray-200 text-gray-700 py-2 rounded-md hover:bg-gray-300"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
