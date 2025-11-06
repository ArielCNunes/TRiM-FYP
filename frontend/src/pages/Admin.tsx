import { useEffect, useState } from "react";
import { useAppSelector } from "../store/hooks";
import { useNavigate } from "react-router-dom";
import { servicesApi, barbersApi, dashboardApi } from "../api/endpoints";
import type { Service, Barber, DashboardStats } from "../types";

/**
 * Type definition for admin dashboard tab navigation
 */
type AdminTab = "dashboard" | "services" | "barbers";

/**
 * Admin Dashboard
 *
 * Allows admins to:
 * 1. Create and manage services
 * 2. Create and manage barbers
 * 3. View all services and barbers in the system
 */
export default function Admin() {
  const navigate = useNavigate();
  const user = useAppSelector((state) => state.auth.user);

  // Verify admin role - redirect if unauthorized
  if (!user || user.role !== "ADMIN") {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Access denied. Admin role required.</p>
        <button
          onClick={() => navigate("/")}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md"
        >
          Go Home
        </button>
      </div>
    );
  }

  const [activeTab, setActiveTab] = useState<AdminTab>("dashboard");

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        {/* Page header */}
        <h1 className="text-4xl font-bold mb-8">Admin Dashboard</h1>

        {/* Tab Navigation */}
        <div className="flex gap-4 mb-8 border-b">
          <button
            onClick={() => setActiveTab("dashboard")}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === "dashboard"
                ? "border-b-2 border-blue-600 text-blue-600"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Dashboard
          </button>
          <button
            onClick={() => setActiveTab("services")}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === "services"
                ? "border-b-2 border-blue-600 text-blue-600"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Services
          </button>
          <button
            onClick={() => setActiveTab("barbers")}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === "barbers"
                ? "border-b-2 border-blue-600 text-blue-600"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Barbers
          </button>
        </div>

        {/* Dashboard Tab */}
        {activeTab === "dashboard" && <DashboardSection />}

        {/* Services Tab */}
        {activeTab === "services" && <ServicesSection />}

        {/* Barbers Tab */}
        {activeTab === "barbers" && <BarbersSection />}
      </div>
    </div>
  );
}

/**
 * Dashboard Section - Shows admin statistics and overview
 */
function DashboardSection() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const response = await dashboardApi.getAdminStats();
      setStats(response.data);
      setError(null);
    } catch (err) {
      setError("Failed to load dashboard statistics");
      console.error("Error fetching stats:", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">Loading dashboard...</p>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">{error || "Failed to load statistics"}</p>
        <button
          onClick={fetchStats}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Key Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">Total Bookings</p>
          <p className="text-3xl font-bold text-gray-900">{stats.totalBookings}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">Today's Bookings</p>
          <p className="text-3xl font-bold text-blue-600">{stats.todaysBookings}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">Active Customers</p>
          <p className="text-3xl font-bold text-green-600">{stats.activeCustomers}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">Active Barbers</p>
          <p className="text-3xl font-bold text-purple-600">{stats.activeBarbers}</p>
        </div>
      </div>

      {/* Revenue Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">Total Revenue</p>
          <p className="text-3xl font-bold text-gray-900">
            €{stats.totalRevenue.toFixed(2)}
          </p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-1">This Month Revenue</p>
          <p className="text-3xl font-bold text-blue-600">
            €{stats.thisMonthRevenue.toFixed(2)}
          </p>
        </div>
      </div>

      {/* Popular Services */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-xl font-bold mb-4">Popular Services</h3>
        {stats.popularServices.length > 0 ? (
          <div className="space-y-2">
            {stats.popularServices.map((service, index) => (
              <div
                key={index}
                className="flex justify-between items-center p-3 bg-gray-50 rounded"
              >
                <span className="font-medium text-gray-900">{service.name}</span>
                <span className="text-sm text-gray-600">
                  {service.count} bookings
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500 text-center py-4">No services data yet</p>
        )}
      </div>

      {/* Recent Bookings */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-xl font-bold mb-4">Recent Bookings</h3>
        {stats.recentBookings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Customer
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Barber
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Service
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Date
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Time
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody>
                {stats.recentBookings.map((booking, index) => (
                  <tr key={index} className="border-t">
                    <td className="px-4 py-3 text-sm text-gray-900">
                      {booking.customerName}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-900">
                      {booking.barberName}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-900">
                      {booking.serviceName}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      {new Date(booking.date).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">{booking.time}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-2 py-1 rounded text-xs font-semibold ${
                          booking.status === "COMPLETED"
                            ? "bg-green-100 text-green-800"
                            : booking.status === "CONFIRMED"
                            ? "bg-blue-100 text-blue-800"
                            : booking.status === "PENDING"
                            ? "bg-yellow-100 text-yellow-800"
                            : booking.status === "CANCELLED"
                            ? "bg-red-100 text-red-800"
                            : "bg-gray-100 text-gray-800"
                        }`}
                      >
                        {booking.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-500 text-center py-4">No recent bookings</p>
        )}
      </div>
    </div>
  );
}

/**
 * Services Management Section
 *
 * Handles CRUD operations for services:
 * - Display list of all active services
 * - Create new services with form validation
 * - Shows service details (name, description, duration, price)
 */
function ServicesSection() {
  // State for services list and UI controls
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  // Form data for creating new services
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    durationMinutes: 30,
    price: 0,
    depositPercentage: 50,
  });

  // Fetch services on component mount
  useEffect(() => {
    fetchServices();
  }, []);

  /**
   * Fetch all active services from the API
   */
  const fetchServices = async () => {
    try {
      const response = await servicesApi.getActive();
      setServices(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load services" });
    }
  };

  /**
   * Handle service creation form submission
   * @param e - Form submit event
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Create new service with validated form data
      await servicesApi.create({
        name: formData.name,
        description: formData.description,
        durationMinutes: parseInt(String(formData.durationMinutes)),
        price: parseFloat(String(formData.price)),
        depositPercentage: parseInt(String(formData.depositPercentage)),
        active: true,
      } as any);

      // Reset form and refresh services list
      setFormData({
        name: "",
        description: "",
        durationMinutes: 30,
        price: 0,
        depositPercentage: 50,
      });
      setShowForm(false);
      setStatus({ type: "success", message: "Service created successfully" });
      fetchServices();
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to create service";
      setStatus({ type: "error", message: String(message) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Section header with add service button */}
      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-2xl font-bold">Services</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          {showForm ? "Cancel" : "Add Service"}
        </button>
      </div>

      {/* Add Service Form - Collapsible */}
      {showForm && (
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

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? "Creating..." : "Create Service"}
            </button>
          </form>
        </div>
      )}

      {/* Services List - Grid layout for service cards */}
      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
            status.type === "success"
              ? "border-green-200 bg-green-50 text-green-700"
              : "border-red-200 bg-red-50 text-red-700"
          }`}
        >
          {status.message}
        </div>
      )}

      {/* Services List - Grid layout for service cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {services.map((service) => (
          <div key={service.id} className="p-4 bg-white rounded-lg shadow">
            <h3 className="text-lg font-bold mb-2">{service.name}</h3>
            <p className="text-gray-600 mb-3 text-sm">{service.description}</p>
            <div className="space-y-1 text-sm text-gray-500">
              <div className="flex justify-between">
                <span>Duration:</span>
                <span>{service.durationMinutes} min</span>
              </div>
              <div className="flex justify-between">
                <span>Price:</span>
                <span className="font-semibold">
                  €{service.price.toFixed(2)}
                </span>
              </div>
              <div className="flex justify-between">
                <span>Deposit:</span>
                <span>{service.depositPercentage}%</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Empty state message */}
      {services.length === 0 && (
        <p className="text-center text-gray-500">
          No services yet. Create one to get started.
        </p>
      )}
    </div>
  );
}

/**
 * Barbers Management Section
 *
 * Handles CRUD operations for barbers:
 * - Display list of all active barbers
 * - Create new barber accounts with user credentials
 * - Shows barber details (name, email, phone, bio)
 */
function BarbersSection() {
  // State for barbers list and UI controls
  const [barbers, setBarbers] = useState<Barber[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  // Form data for creating new barbers
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    bio: "",
  });

  // Fetch barbers on component mount
  useEffect(() => {
    fetchBarbers();
  }, []);

  /**
   * Fetch all active barbers from the API
   */
  const fetchBarbers = async () => {
    try {
      const response = await barbersApi.getActive();
      setBarbers(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load barbers" });
    }
  };

  /**
   * Handle barber creation form submission
   * Creates a new barber account with associated user credentials
   * @param e - Form submit event
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Create new barber with user account
      await barbersApi.create(formData as any);

      // Reset form and refresh barbers list
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        password: "",
        bio: "",
      });
      setShowForm(false);
      setStatus({ type: "success", message: "Barber created successfully" });
      fetchBarbers();
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to create barber";
      setStatus({ type: "error", message: String(message) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Section header with add barber button */}
      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-2xl font-bold">Barbers</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          {showForm ? "Cancel" : "Add Barber"}
        </button>
      </div>

      {/* Add Barber Form - Collapsible */}
      {showForm && (
        <div className="mb-8 p-6 bg-white rounded-lg shadow">
          <h3 className="text-xl font-bold mb-4">Create New Barber</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">
                  First Name
                </label>
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
                <label className="block text-sm font-medium mb-1">
                  Last Name
                </label>
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

            <div>
              <label className="block text-sm font-medium mb-1">Phone</label>
              <input
                type="tel"
                required
                pattern="[0-9]{10,15}"
                className="w-full border rounded-md p-2"
                value={formData.phone}
                onChange={(e) =>
                  setFormData({ ...formData, phone: e.target.value })
                }
                placeholder="353871234567 (digits only, 10-15 chars)"
              />
              <p className="text-xs text-gray-500 mt-1">
                Enter digits only (no spaces or dashes), 10-15 characters
              </p>
            </div>

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
                onChange={(e) =>
                  setFormData({ ...formData, bio: e.target.value })
                }
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? "Creating..." : "Create Barber"}
            </button>
          </form>
        </div>
      )}

      {/* Barbers List - Grid layout for barber cards */}
      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
            status.type === "success"
              ? "border-green-200 bg-green-50 text-green-700"
              : "border-red-200 bg-red-50 text-red-700"
          }`}
        >
          {status.message}
        </div>
      )}

      {/* Barbers List - Grid layout for barber cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {barbers.map((barber) => (
          <div key={barber.id} className="p-4 bg-white rounded-lg shadow">
            <h3 className="text-lg font-bold mb-1">
              {barber.user.firstName} {barber.user.lastName}
            </h3>
            <p className="text-sm text-gray-600 mb-2">{barber.user.email}</p>
            {barber.bio && (
              <p className="text-sm text-gray-700 mb-2">{barber.bio}</p>
            )}
            <p className="text-xs text-gray-500">{barber.user.phone}</p>
          </div>
        ))}
      </div>

      {/* Empty state message */}
      {barbers.length === 0 && (
        <p className="text-center text-gray-500">
          No barbers yet. Create one to get started.
        </p>
      )}
    </div>
  );
}
