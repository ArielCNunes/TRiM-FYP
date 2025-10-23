import { useEffect, useState } from 'react';
import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';
import { servicesApi, barbersApi } from '../api/endpoints';
import type { Service, Barber } from '../types';

/**
 * Type definition for admin dashboard tab navigation
 */
type AdminTab = 'services' | 'barbers';

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
  const user = useAppSelector(state => state.auth.user);

  // Verify admin role - redirect if unauthorized
  if (!user || user.role !== 'ADMIN') {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Access denied. Admin role required.</p>
        <button
          onClick={() => navigate('/')}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md"
        >
          Go Home
        </button>
      </div>
    );
  }

  const [activeTab, setActiveTab] = useState<AdminTab>('services');

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        {/* Page header */}
        <h1 className="text-4xl font-bold mb-8">Admin Dashboard</h1>

        {/* Tab Navigation */}
        <div className="flex gap-4 mb-8 border-b">
          <button
            onClick={() => setActiveTab('services')}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === 'services'
                ? 'border-b-2 border-blue-600 text-blue-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Services
          </button>
          <button
            onClick={() => setActiveTab('barbers')}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === 'barbers'
                ? 'border-b-2 border-blue-600 text-blue-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Barbers
          </button>
        </div>

        {/* Services Tab */}
        {activeTab === 'services' && <ServicesSection />}

        {/* Barbers Tab */}
        {activeTab === 'barbers' && <BarbersSection />}
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
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  
  // Form data for creating new services
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    durationMinutes: 30,
    price: 0,
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
      setStatus({ type: 'error', message: 'Failed to load services' });
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
        active: true,
      } as any);
      
      // Reset form and refresh services list
      setFormData({ name: '', description: '', durationMinutes: 30, price: 0 });
      setShowForm(false);
      setStatus({ type: 'success', message: 'Service created successfully' });
      fetchServices();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create service';
      setStatus({ type: 'error', message: String(message) });
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
          {showForm ? 'Cancel' : 'Add Service'}
        </button>
      </div>

      {/* Add Service Form - Collapsible */}
      {showForm && (
        <div className="mb-8 p-6 bg-white rounded-lg shadow">
          <h3 className="text-xl font-bold mb-4">Create New Service</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">Service Name</label>
              <input
                type="text"
                required
                className="w-full border rounded-md p-2"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Description</label>
              <textarea
                required
                className="w-full border rounded-md p-2"
                rows={3}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">Duration (minutes)</label>
                <input
                  type="number"
                  required
                  min="15"
                  step="15"
                  className="w-full border rounded-md p-2"
                  value={formData.durationMinutes}
                  onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })}
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Price (€)</label>
                <input
                  type="number"
                  required
                  min="0"
                  step="0.01"
                  className="w-full border rounded-md p-2"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? 'Creating...' : 'Create Service'}
            </button>
          </form>
        </div>
      )}

      {/* Services List - Grid layout for service cards */}
      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
            status.type === 'success'
              ? 'border-green-200 bg-green-50 text-green-700'
              : 'border-red-200 bg-red-50 text-red-700'
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
            <div className="flex justify-between text-sm text-gray-500">
              <span>{service.durationMinutes} min</span>
              <span className="font-semibold">€{service.price.toFixed(2)}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Empty state message */}
      {services.length === 0 && (
        <p className="text-center text-gray-500">No services yet. Create one to get started.</p>
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
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  
  // Form data for creating new barbers
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    bio: '',
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
      setStatus({ type: 'error', message: 'Failed to load barbers' });
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
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        password: '',
        bio: '',
      });
      setShowForm(false);
      setStatus({ type: 'success', message: 'Barber created successfully' });
      fetchBarbers();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create barber';
      setStatus({ type: 'error', message: String(message) });
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
          {showForm ? 'Cancel' : 'Add Barber'}
        </button>
      </div>

      {/* Add Barber Form - Collapsible */}
      {showForm && (
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
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Last Name</label>
                <input
                  type="text"
                  required
                  className="w-full border rounded-md p-2"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
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
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Phone</label>
              <input
                type="tel"
                required
                className="w-full border rounded-md p-2"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Password</label>
              <input
                type="password"
                required
                minLength={6}
                className="w-full border rounded-md p-2"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
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

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? 'Creating...' : 'Create Barber'}
            </button>
          </form>
        </div>
      )}

      {/* Barbers List - Grid layout for barber cards */}
      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
            status.type === 'success'
              ? 'border-green-200 bg-green-50 text-green-700'
              : 'border-red-200 bg-red-50 text-red-700'
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
            {barber.bio && <p className="text-sm text-gray-700 mb-2">{barber.bio}</p>}
            <p className="text-xs text-gray-500">{barber.user.phone}</p>
          </div>
        ))}
      </div>

      {/* Empty state message */}
      {barbers.length === 0 && (
        <p className="text-center text-gray-500">No barbers yet. Create one to get started.</p>
      )}
    </div>
  );
}