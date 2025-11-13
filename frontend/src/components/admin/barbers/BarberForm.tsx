import { useState } from "react";
import { barbersApi } from "../../../api/endpoints";
import { PhoneInput } from "../../shared/PhoneInput";

interface BarberFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export default function BarberForm({ onSuccess, onCancel }: BarberFormProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    bio: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await barbersApi.create(formData as any);

      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        password: "",
        bio: "",
      });
      onSuccess();
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to create barber";
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mb-8 p-6 bg-white rounded-lg shadow">
      <h3 className="text-xl font-bold mb-4">Create New Barber</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">First Name</label>
            <input
              type="text"
              required
              className="w-full border rounded-md p-2"
              value={formData.firstName}
              onChange={(e) =>
                setFormData({ ...formData, firstName: e.target.value })
              }
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Last Name</label>
            <input
              type="text"
              required
              className="w-full border rounded-md p-2"
              value={formData.lastName}
              onChange={(e) =>
                setFormData({ ...formData, lastName: e.target.value })
              }
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Email</label>
          <input
            type="email"
            required
            className="w-full border rounded-md p-2"
            value={formData.email}
            onChange={(e) =>
              setFormData({ ...formData, email: e.target.value })
            }
          />
        </div>

        <PhoneInput
          value={formData.phone}
          onChange={(normalizedPhone) =>
            setFormData({ ...formData, phone: normalizedPhone })
          }
          label="Phone"
          required
        />

        <div>
          <label className="block text-sm font-medium mb-1">Password</label>
          <input
            type="password"
            required
            minLength={8}
            className="w-full border rounded-md p-2"
            value={formData.password}
            onChange={(e) =>
              setFormData({ ...formData, password: e.target.value })
            }
            placeholder="Minimum 8 characters"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Bio</label>
          <textarea
            className="w-full border rounded-md p-2"
            rows={3}
            value={formData.bio}
            onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
          />
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {loading ? "Creating..." : "Create Barber"}
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
