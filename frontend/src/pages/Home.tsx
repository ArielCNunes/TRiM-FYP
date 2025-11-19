import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { Settings, Users, Calendar, Clock, Scissors, Star } from "lucide-react";

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div className="min-h-screen bg-zinc-950">
      {/* Admin View */}
      {user?.role === "ADMIN" && (
        <div className="max-w-7xl mx-auto px-6 py-20">
          <div className="text-center mb-16">
            <h1 className="text-6xl font-bold mb-6 text-white">TRiM Admin</h1>
            <p className="text-xl text-zinc-400 mb-8">
              Manage your barbershop operations
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Services Management */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition">
              <div className="flex items-center mb-4">
                <div className="bg-indigo-900/30 p-3 rounded-lg">
                  <Settings className="w-8 h-8 text-indigo-400" />
                </div>
                <h2 className="text-2xl font-bold text-white ml-4">Services</h2>
              </div>
              <p className="text-zinc-400 mb-6">Create and manage services</p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-indigo-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-indigo-500 transition"
              >
                Manage Services
              </button>
            </div>

            {/* Barbers Management */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition">
              <div className="flex items-center mb-4">
                <div className="bg-emerald-900/30 p-3 rounded-lg">
                  <Users className="w-8 h-8 text-emerald-400" />
                </div>
                <h2 className="text-2xl font-bold text-white ml-4">Barbers</h2>
              </div>
              <p className="text-zinc-400 mb-6">
                Add new barbers, manage profiles, and configure their
                availability
              </p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-emerald-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-emerald-500 transition"
              >
                Manage Barbers
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Barber View */}
      {user?.role === "BARBER" && (
        <div className="max-w-7xl mx-auto px-6 py-20">
          <div className="text-center mb-16">
            <h1 className="text-6xl font-bold mb-6 text-white">
              Welcome, {user.firstName}!
            </h1>
            <p className="text-xl text-zinc-400 mb-8">Your barber dashboard</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Today's Schedule */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition">
              <div className="flex items-center mb-4">
                <div className="bg-indigo-900/30 p-3 rounded-lg">
                  <Calendar className="w-8 h-8 text-indigo-400" />
                </div>
                <h2 className="text-2xl font-bold text-white ml-4">
                  Today's Schedule
                </h2>
              </div>
              <p className="text-zinc-400 mb-6">
                View and manage your appointments for today
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-indigo-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-indigo-500 transition"
              >
                View Schedule
              </button>
            </div>

            {/* Availability */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition">
              <div className="flex items-center mb-4">
                <div className="bg-emerald-900/30 p-3 rounded-lg">
                  <Clock className="w-8 h-8 text-emerald-400" />
                </div>
                <h2 className="text-2xl font-bold text-white ml-4">
                  Availability
                </h2>
              </div>
              <p className="text-zinc-400 mb-6">
                Set your working hours and manage your schedule
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-emerald-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-emerald-500 transition"
              >
                Manage Availability
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Customer/Guest View */}
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
                <p className="text-2xl text-zinc-300 mb-4">
                  Professional Barbershop Services
                </p>
                <p className="text-lg text-zinc-500 mb-12 max-w-2xl mx-auto">
                  Book your appointment with us.
                </p>

                <button
                  onClick={() => navigate("/booking")}
                  className="bg-indigo-600 text-white px-16 py-5 rounded-lg font-semibold hover:bg-indigo-500 transition text-xl shadow-lg shadow-indigo-500/20"
                >
                  Book Your Appointment
                </button>

                {/* Features Section */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20">
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
