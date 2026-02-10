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
    <div className="min-h-screen bg-[var(--bg-base)]">
      {/* Admin View - Calendar is the main feature */}
      {user?.role === "ADMIN" && (
        <div className="w-full px-8 py-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-[var(--text-primary)]">Shop Calendar</h1>
              <p className="text-[var(--text-muted)]">View and manage all bookings</p>
            </div>
            <button
              onClick={() => navigate("/admin")}
              className="bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-lg font-medium hover:bg-[var(--bg-muted)] transition flex items-center gap-2"
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
              <h1 className="text-3xl font-bold text-[var(--text-primary)]">My Schedule</h1>
              <p className="text-[var(--text-muted)]">View and manage your bookings</p>
            </div>
            <button
              onClick={() => navigate("/barber")}
              className="bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-lg font-medium hover:bg-[var(--bg-muted)] transition flex items-center gap-2"
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
                <h1 className="text-7xl font-bold mb-6 text-[var(--text-primary)] tracking-tight">
                  TRiM
                </h1>
                {isSubdomain ? (
                  // Subdomain landing - for customers
                  <>
                    <p className="text-2xl text-[var(--text-secondary)] mb-4">
                      Professional Barbershop Services
                    </p>
                    <p className="text-lg text-[var(--text-subtle)] mb-12 max-w-2xl mx-auto">
                      Sign in to book appointments and manage your bookings.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 justify-center mb-20">
                      <button
                        onClick={() => navigate("/auth")}
                        className="bg-[var(--accent)] text-[var(--text-primary)] px-12 py-5 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-xl shadow-lg shadow-[var(--accent-shadow)]"
                      >
                        Sign In
                      </button>
                      <button
                        onClick={() => navigate("/auth?tab=signup")}
                        className="bg-transparent border-2 border-[var(--focus-ring)] text-[var(--accent-text)] px-12 py-5 rounded-lg font-semibold hover:bg-indigo-950 transition text-xl"
                      >
                        Create Account
                      </button>
                    </div>

                    {/* Features Section - Customer focused */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-indigo-900/30 p-4 rounded-lg">
                            <Scissors className="w-10 h-10 text-[var(--accent-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Expert Barbers
                        </h3>
                        <p className="text-[var(--text-muted)]">
                          Skilled professionals dedicated to your perfect look
                        </p>
                      </div>
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-emerald-900/30 p-4 rounded-lg">
                            <Calendar className="w-10 h-10 text-[var(--success-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Easy Booking
                        </h3>
                        <p className="text-[var(--text-muted)]">
                          Book online anytime, manage appointments effortlessly
                        </p>
                      </div>
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-amber-900/30 p-4 rounded-lg">
                            <Star className="w-10 h-10 text-[var(--warning-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Premium Service
                        </h3>
                        <p className="text-[var(--text-muted)]">
                          Quality cuts and styling for every occasion
                        </p>
                      </div>
                    </div>
                  </>
                ) : (
                  // Main domain landing - for businesses
                  <>
                    <p className="text-2xl text-[var(--text-secondary)] mb-4">
                      The Modern Barbershop Booking Platform
                    </p>
                    <p className="text-lg text-[var(--text-subtle)] mb-12 max-w-2xl mx-auto">
                      Streamline your barbershop operations with powerful booking, scheduling, and management tools.
                    </p>

                    <button
                      onClick={() => navigate("/register-business")}
                      className="bg-[var(--accent)] text-[var(--text-primary)] px-16 py-5 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-xl shadow-lg shadow-[var(--accent-shadow)] mb-20"
                    >
                      Register Your Business
                    </button>

                    {/* Features Section - Business focused */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-indigo-900/30 p-4 rounded-lg">
                            <Scissors className="w-10 h-10 text-[var(--accent-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Team Management
                        </h3>
                        <p className="text-[var(--text-muted)]">
                          Add barbers, set their schedules, and manage availability effortlessly
                        </p>
                      </div>
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-emerald-900/30 p-4 rounded-lg">
                            <Calendar className="w-10 h-10 text-[var(--success-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Online Booking
                        </h3>
                        <p className="text-[var(--text-muted)]">
                          Let customers book appointments 24/7 through your custom booking page
                        </p>
                      </div>
                      <div className="bg-[var(--bg-surface)] border border-[var(--border-subtle)] rounded-xl p-8">
                        <div className="flex items-center justify-center mb-4">
                          <div className="bg-amber-900/30 p-4 rounded-lg">
                            <Star className="w-10 h-10 text-[var(--warning-text)]" />
                          </div>
                        </div>
                        <h3 className="text-xl font-bold mb-2 text-[var(--text-primary)]">
                          Integrated Payments
                        </h3>
                        <p className="text-[var(--text-muted)]">
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
                  <h1 className="text-6xl font-bold mb-6 text-[var(--text-primary)]">
                    Welcome back, {user?.firstName}.
                  </h1>
                  <p className="text-xl text-[var(--text-muted)] mb-8">
                    Ready for your next appointment?
                  </p>

                  <div className="flex gap-4 justify-center">
                    <button
                      onClick={() => navigate("/booking")}
                      className="bg-[var(--accent)] text-[var(--text-primary)] px-12 py-4 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-lg shadow-lg shadow-[var(--accent-shadow)]"
                    >
                      Book New Appointment
                    </button>
                    <button
                      onClick={() => navigate("/my-bookings")}
                      className="bg-transparent border-2 border-[var(--focus-ring)] text-[var(--accent-text)] px-12 py-4 rounded-lg font-semibold hover:bg-indigo-950 transition text-lg"
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
