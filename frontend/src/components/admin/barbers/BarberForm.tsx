import { useState, useEffect } from "react";
import { barbersApi } from "../../../api/endpoints";
import { PhoneInput } from "../../shared/PhoneInput";
import type { Barber } from "../../../types";

interface BarberFormProps {
  editingBarber?: Barber | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function BarberForm({ editingBarber, onSuccess, onCancel }: BarberFormProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    bio: "",
    profileImageUrl: "",
  });

  useEffect(() => {
    if (editingBarber) {
      setFormData({
        firstName: editingBarber.user.firstName,
        lastName: editingBarber.user.lastName,
        email: editingBarber.user.email,
        phone: editingBarber.user.phone,
        password: "",
        bio: editingBarber.bio || "",
        profileImageUrl: editingBarber.profileImageUrl || "",
      });
    }
  }, [editingBarber]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (editingBarber) {
        await barbersApi.update(editingBarber.id, {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          bio: formData.bio,
          profileImageUrl: formData.profileImageUrl || undefined,
        });
      } else {
        // Create barber and get the response with the new barber's ID
        const response = await barbersApi.create(formData as any);
        const newBarberId = response.data.id;

        // Save default availability for the new barber
        const defaultAvailability = [
          { day: "MONDAY", start: "09:00", end: "17:00", enabled: true },
          { day: "TUESDAY", start: "09:00", end: "17:00", enabled: true },
          { day: "WEDNESDAY", start: "09:00", end: "17:00", enabled: true },
          { day: "THURSDAY", start: "09:00", end: "17:00", enabled: true },
          { day: "FRIDAY", start: "09:00", end: "17:00", enabled: true },
          { day: "SATURDAY", start: "10:00", end: "16:00", enabled: false },
          { day: "SUNDAY", start: "09:00", end: "17:00", enabled: false },
        ];

        // Save each day's availability
        for (const slot of defaultAvailability) {
          await barbersApi.setAvailability({
            barberId: newBarberId,
            dayOfWeek: slot.day,
            startTime: slot.start,
            endTime: slot.end,
            isAvailable: slot.enabled,
          });
        }
      }

      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        password: "",
        bio: "",
        profileImageUrl: "",
      });
      onSuccess();
    } catch (err: any) {
      const message =
        err.response?.data?.message || `Failed to ${editingBarber ? "update" : "create"} barber`;
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mb-8 p-6 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <h3 className="text-xl font-bold mb-4 text-white">
        {editingBarber ? "Edit Barber" : "Create New Barber"}
      </h3>

      {error && (
        <div className="mb-4 p-3 bg-red-900/50 border border-red-800 rounded text-red-300 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name fields - always editable */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1 text-zinc-300">
              First Name
            </label>
            <input
              type="text"
              required
              className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              value={formData.firstName}
              onChange={(e) =>
                setFormData({ ...formData, firstName: e.target.value })
              }
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1 text-zinc-300">
              Last Name
            </label>
            <input
              type="text"
              required
              className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              value={formData.lastName}
              onChange={(e) =>
                setFormData({ ...formData, lastName: e.target.value })
              }
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Email
          </label>
          <input
            type="email"
            required
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
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

        {/* Password only shown when creating a new barber */}
        {!editingBarber && (
          <div>
            <label className="block text-sm font-medium mb-1 text-zinc-300">
              Password
            </label>
            <input
              type="password"
              required
              minLength={8}
              className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              value={formData.password}
              onChange={(e) =>
                setFormData({ ...formData, password: e.target.value })
              }
              placeholder="Minimum 8 characters"
            />
          </div>
        )}

        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Bio
          </label>
          <textarea
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            rows={3}
            value={formData.bio}
            onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
            placeholder="Brief description of experience and specialties"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-zinc-300">
            Profile Image URL
          </label>
          <input
            type="url"
            className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={formData.profileImageUrl}
            onChange={(e) =>
              setFormData({ ...formData, profileImageUrl: e.target.value })
            }
            placeholder="https://example.com/photo.jpg"
          />
          <p className="text-xs text-zinc-500 mt-1">
            Optional: URL to the barber's profile photo
          </p>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 transition"
          >
            {loading
              ? editingBarber
                ? "Updating..."
                : "Creating..."
              : editingBarber
                ? "Update Barber"
                : "Create Barber"}
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
