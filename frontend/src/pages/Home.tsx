import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { Settings, Calendar, Scissors, Star } from "lucide-react";
import AdminCalendar from "../components/admin/calendar/AdminCalendar";
import BarberCalendar from "../components/barber/BarberCalendar";
import { getBusinessSlug } from "../api/axios";

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

  const slug = getBusinessSlug();
  const businessName = slug
    ? slug.split("-").map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(" ")
    : null;

  return (
    <div className="min-h-screen bg-[var(--bg-base)]">
      {/* Admin View - Calendar is the main feature */}
      {user?.role === "ADMIN" && (
        <div className="w-full px-4 py-6 md:px-8 md:py-8">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-[var(--text-primary)]">Shop Calendar</h1>
              <p className="text-[var(--text-muted)]">View and manage all bookings</p>
            </div>
            <button
              onClick={() => navigate("/admin")}
              className="bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-lg font-medium hover:bg-[var(--bg-muted)] transition flex items-center gap-2 self-start"
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
        <div className="w-full px-4 py-6 md:px-8 md:py-8">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-[var(--text-primary)]">My Schedule</h1>
              <p className="text-[var(--text-muted)]">View and manage your bookings</p>
            </div>
            <button
              onClick={() => navigate("/barber")}
              className="bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-lg font-medium hover:bg-[var(--bg-muted)] transition flex items-center gap-2 self-start"
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
          <div className="max-w-7xl mx-auto px-4 py-12 md:px-6 md:py-20">
            {!isAuthenticated ? (
              // Public Landing Page
              <div className="text-center">
                <h1 className="text-5xl md:text-7xl font-bold mb-6 text-[var(--text-primary)] tracking-tight">
                  TRiM
                </h1>
                {isSubdomain ? (
                  // Subdomain landing for customers
                  <>
                    {businessName && (
                      <p className="text-2xl md:text-3xl font-semibold text-[var(--text-secondary)] mb-2">
                        {businessName}
                      </p>
                    )}
                    <p className="text-base md:text-lg text-[var(--text-subtle)] mb-8 md:mb-12 max-w-2xl mx-auto">
                      Sign in to book appointments and manage your bookings.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12 md:mb-20">
                      <button
                        onClick={() => navigate("/auth")}
                        className="bg-[var(--accent)] text-white px-8 py-4 md:px-12 md:py-5 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-lg md:text-xl shadow-lg shadow-[var(--accent-shadow)]"
                      >
                        Sign In
                      </button>
                      <button
                        onClick={() => navigate("/auth?tab=signup")}
                        className="bg-transparent border-2 border-[var(--focus-ring)] text-[var(--accent-text)] px-8 py-4 md:px-12 md:py-5 rounded-lg font-semibold hover:bg-indigo-950 transition text-lg md:text-xl"
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
                    <p className="text-xl md:text-2xl text-[var(--text-secondary)] mb-4">
                      The Modern Barbershop Booking Platform
                    </p>
                    <p className="text-base md:text-lg text-[var(--text-subtle)] mb-8 md:mb-12 max-w-2xl mx-auto">
                      Streamline your barbershop operations with powerful booking, scheduling, and management tools.
                    </p>

                    <button
                      onClick={() => navigate("/register-business")}
                      className="bg-[var(--accent)] text-white px-8 py-4 md:px-16 md:py-5 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-lg md:text-xl shadow-lg shadow-[var(--accent-shadow)] mb-12 md:mb-20"
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
                          Accept online payments securely.
                        </p>
                      </div>
                    </div>
                  </>
                )}
              </div>
            ) : (
              // Logged-in Customer View
              <>
                <div className="text-center mb-12 md:mb-16">
                  <h1 className="text-4xl md:text-6xl font-bold mb-6 text-[var(--text-primary)]">
                    Welcome back, {user?.firstName}.
                  </h1>
                  <p className="text-lg md:text-xl text-[var(--text-muted)] mb-8">
                    Ready for your next appointment?
                  </p>

                  <div className="flex flex-col sm:flex-row gap-4 justify-center">
                    <button
                      onClick={() => navigate("/booking")}
                      className="bg-[var(--accent)] text-white px-8 py-3 md:px-12 md:py-4 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition text-base md:text-lg shadow-lg shadow-[var(--accent-shadow)]"
                    >
                      Book New Appointment
                    </button>
                    <button
                      onClick={() => navigate("/my-bookings")}
                      className="bg-transparent border-2 border-[var(--focus-ring)] text-[var(--accent-text)] px-8 py-3 md:px-12 md:py-4 rounded-lg font-semibold hover:bg-indigo-950 transition text-base md:text-lg"
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
