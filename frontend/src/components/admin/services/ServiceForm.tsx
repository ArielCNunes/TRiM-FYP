import { useState, useEffect } from "react";
import { servicesApi, categoriesApi } from "../../../api/endpoints";
import type { Service, ServiceCategory } from "../../../types";

interface ServiceFormProps {
  editingService?: Service | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function ServiceForm({
  editingService,
  onSuccess,
  onCancel,
}: ServiceFormProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<ServiceCategory[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    durationMinutes: 30,
    price: 0,
    depositPercentage: 50,
    categoryId: 0,
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    if (editingService) {
      setFormData({
        name: editingService.name,
        description: editingService.description || "",
        durationMinutes: editingService.durationMinutes,
        price: editingService.price,
        depositPercentage: editingService.depositPercentage,
        categoryId: editingService.categoryId || 0,
      });
    }
  }, [editingService]);

  const fetchCategories = async () => {
    try {
      const response = await categoriesApi.getAll();
      setCategories(response.data);
      // Set default category if available and not editing
      if (response.data.length > 0 && !editingService) {
        setFormData((prev) => ({ ...prev, categoryId: response.data[0].id }));
      }
    } catch (err) {
      console.error("Failed to fetch categories", err);
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const serviceData = {
        name: formData.name,
        description: formData.description,
        durationMinutes: parseInt(String(formData.durationMinutes)),
        price: parseFloat(String(formData.price)),
        depositPercentage: parseInt(String(formData.depositPercentage)),
        active: true,
        categoryId: formData.categoryId,
      };

      if (editingService) {
        await servicesApi.update(editingService.id, serviceData);
      } else {
        await servicesApi.create(serviceData);
      }

      setFormData({
        name: "",
        description: "",
        durationMinutes: 30,
        price: 0,
        depositPercentage: 50,
        categoryId: categories.length > 0 ? categories[0].id : 0,
      });
      onSuccess();
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        `Failed to ${editingService ? "update" : "create"} service`;
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  if (loadingCategories) {
    return (
      <div className="mb-8 p-6 bg-zinc-900 rounded-lg shadow border border-zinc-800">
        <p className="text-zinc-400">Loading categories...</p>
      </div>
    );
  }

  if (categories.length === 0) {
    return (
      <div className="mb-8 p-6 bg-zinc-900 rounded-lg shadow border border-zinc-800">
        <p className="text-amber-400">Please create a category first before adding services.</p>
        <button
          type="button"
          onClick={onCancel}
          className="mt-4 px-6 bg-zinc-800 text-zinc-300 py-2 rounded-md hover:bg-zinc-700 transition border border-zinc-700"
        >
          Cancel
        </button>
      </div>
    );
  }

  return (
    <div className="mb-8 p-6 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <h3 className="text-xl font-bold mb-4 text-white">
        {editingService ? "Edit Service" : "Create New Service"}
      </h3>

      {error && (
        <div className="mb-4 p-3 bg-red-900/50 border border-red-800 rounded text-red-300 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Category
          </label>
          <select
            required
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={formData.categoryId}
            onChange={(e) =>
              setFormData({ ...formData, categoryId: parseInt(e.target.value) })
            }
          >
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Service Name
          </label>
          <input
            type="text"
            required
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Description
          </label>
          <textarea
            required
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            rows={3}
            value={formData.description}
            onChange={(e) =>
              setFormData({ ...formData, description: e.target.value })
            }
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1 text-zinc-300">
              Duration (minutes)
            </label>
            <input
              type="number"
              required
              min="15"
              step="15"
              className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
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
            <label className="block text-sm font-medium mb-1 text-zinc-300">
              Price (€)
            </label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
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
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Deposit Percentage (%)
          </label>
          <input
            type="number"
            required
            min="0"
            max="100"
            step="1"
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={formData.depositPercentage}
            onChange={(e) =>
              setFormData({
                ...formData,
                depositPercentage: parseInt(e.target.value),
              })
            }
          />
          <p className="text-xs text-zinc-500 mt-1">
            Percentage of the total price required as deposit (0-100%)
          </p>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 transition"
          >
            {loading
              ? editingService
                ? "Updating..."
                : "Creating..."
              : editingService
                ? "Update Service"
                : "Create Service"}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="px-6 bg-zinc-800 text-zinc-300 py-2 rounded-md hover:bg-zinc-700 transition border border-zinc-700"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
