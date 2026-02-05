import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { Settings, Calendar, Scissors, Star } from "lucide-react";
import AdminCalendar from "../components/admin/calendar/AdminCalendar";
import BarberCalendar from "../components/barber/BarberCalendar";

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const user = useAppSelector((state) => state.auth.user);

  // Detect if we're on a subdomain (e.g., v6.localhost vs localhost)
  const hostname = window.location.hostname;
  const parts = hostname.split(".");
  const isSubdomain =
    (hostname.endsWith(".localhost") && parts.length >= 2) ||
    (!hostname.includes("localhost") && parts.length > 2);

  return (
    <div className="min-h-screen bg-zinc-950">
      {/* Admin View - Calendar is the main feature */}
      {user?.role === "ADMIN" && (
        <div className="w-full px-8 py-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-white">Shop Calendar</h1>
              <p className="text-zinc-400">View and manage all bookings</p>
            </div>
            <button
              onClick={() => navigate("/admin")}
              className="bg-zinc-800 text-white px-4 py-2 rounded-lg font-medium hover:bg-zinc-700 transition flex items-center gap-2"
            >
              <Settings className="w-4 h-4" />
              Admin Settings
            </button>
          </div>

          <AdminCalendar />
        </div>
      )}

      {/* Barber View */}
      {user?.role === "BARBER" && (
        <div className="w-full px-8 py-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-white">My Schedule</h1>
              <p className="text-zinc-400">View and manage your bookings</p>
            </div>
            <button
              onClick={() => navigate("/barber")}
              className="bg-zinc-800 text-white px-4 py-2 rounded-lg font-medium hover:bg-zinc-700 transition flex items-center gap-2"
            >
              <Settings className="w-4 h-4" />
              Manage Availability
            </button>
          </div>

          <BarberCalendar />
        </div>
      )}

      {/* Customer View */}
      {(!user || user.role === "CUSTOMER") && (
        <>
          {/* Hero Section - Landing Page */}
          <div className="max-w-7xl mx-auto px-6 py-20">
            {!isAuthenticated ? (
              // Public Landing Page
              <div className="text-center">
                <h1 className="text-7xl font-bold mb-6 text-white tracking-tight">
                  TRiM
                </h1>
                {isSubdomain ? (
                  // Subdomain landing - for customers
                  <>
                    <p className="text-2xl text-zinc-300 mb-4">
                      Professional Barbershop Services
                    </p>
                    <p className="text-lg text-zinc-500 mb-12 max-w-2xl mx-auto">
                      Sign in to book appointments and manage your bookings.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 justify-center mb-20">
                      <button
                        onClick={() => navigate("/auth")}
                        className="bg-indigo-600 text-white px-12 py-5 rounded-lg font-semibold hover:bg-indigo-500 transition text-xl shadow-lg shadow-indigo-500/20"
                      >
                        Sign In
                      </button>
                      <button
                        onClick={() => navigate("/auth?tab=signup")}
                        className="bg-transparent border-2 border-indigo-500 text-indigo-400 px-12 py-5 rounded-lg font-semibold hover:bg-indigo-950 transition text-xl"
                      >
                        Create Account
                      </button>
                    </div>

                    {/* Features Section - Customer focused */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-indigo-900/30 p-4 rounded-lg">
                            <Scissors className="w-10 h-10 text-indigo-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Expert Barbers
                        </h3>
                        <p className="text-zinc-400">
                          Skilled professionals dedicated to your perfect look
                        </p>
                      </div>
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-emerald-900/30 p-4 rounded-lg">
                            <Calendar className="w-10 h-10 text-emerald-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Easy Booking
                        </h3>
                        <p className="text-zinc-400">
                          Book online anytime, manage appointments effortlessly
                        </p>
                      </div>
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-amber-900/30 p-4 rounded-lg">
                            <Star className="w-10 h-10 text-amber-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Premium Service
                        </h3>
                        <p className="text-zinc-400">
                          Quality cuts and styling for every occasion
                        </p>
                      </div>
                    </div>
                  </>
                ) : (
                  // Main domain landing - for businesses
                  <>
                    <p className="text-2xl text-zinc-300 mb-4">
                      The Modern Barbershop Booking Platform
                    </p>
                    <p className="text-lg text-zinc-500 mb-12 max-w-2xl mx-auto">
                      Streamline your barbershop operations with powerful booking, scheduling, and management tools.
                    </p>

                    <button
                      onClick={() => navigate("/register-business")}
                      className="bg-indigo-600 text-white px-16 py-5 rounded-lg font-semibold hover:bg-indigo-500 transition text-xl shadow-lg shadow-indigo-500/20 mb-20"
                    >
                      Register Your Business
                    </button>

                    {/* Features Section - Business focused */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-indigo-900/30 p-4 rounded-lg">
                            <Scissors className="w-10 h-10 text-indigo-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Team Management
                        </h3>
                        <p className="text-zinc-400">
                          Add barbers, set their schedules, and manage availability effortlessly
                        </p>
                      </div>
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-emerald-900/30 p-4 rounded-lg">
                            <Calendar className="w-10 h-10 text-emerald-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Online Booking
                        </h3>
                        <p className="text-zinc-400">
                          Let customers book appointments 24/7 through your custom booking page
                        </p>
                      </div>
                      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-amber-900/30 p-4 rounded-lg">
                            <Star className="w-10 h-10 text-amber-400" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-white">
                          Integrated Payments
                        </h3>
                        <p className="text-zinc-400">
                          Accept online payments securely with built-in Stripe integration
                        </p>
                      </div>
                    </div>
                  </>
                )}
              </div>
            ) : (
              // Logged-in Customer View
              <>
                <div className="text-center mb-16">
                  <h1 className="text-6xl font-bold mb-6 text-white">
                    Welcome back, {user?.firstName}.
                  </h1>
                  <p className="text-xl text-zinc-400 mb-8">
                    Ready for your next appointment?
                  </p>

                  <div className="flex gap-4 justify-center">
                    <button
                      onClick={() => navigate("/booking")}
                      className="bg-indigo-600 text-white px-12 py-4 rounded-lg font-semibold hover:bg-indigo-500 transition text-lg shadow-lg shadow-indigo-500/20"
                    >
                      Book New Appointment
                    </button>
                    <button
                      onClick={() => navigate("/my-bookings")}
                      className="bg-transparent border-2 border-indigo-500 text-indigo-400 px-12 py-4 rounded-lg font-semibold hover:bg-indigo-950 transition text-lg"
                    >
                      View My Bookings
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </>
      )}
    </div>
  );
}
