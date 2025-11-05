import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { Settings, Users, Calendar, Clock, Scissors, Star } from "lucide-react";

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      {/* Admin View */}
      {user?.role === "ADMIN" && (
        <div className="max-w-7xl mx-auto px-6 py-20">
          <div className="text-center mb-16">
            <h1 className="text-6xl font-bold mb-6 text-gray-900">
              TRiM Admin
            </h1>
            <p className="text-xl text-gray-600 mb-8">
              Manage your barbershop operations
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Services Management */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-blue-100 p-3 rounded-lg">
                  <Settings className="w-8 h-8 text-blue-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Services
                </h2>
              </div>
              <p className="text-gray-600 mb-6">Create and manage services</p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                Manage Services
              </button>
            </div>

            {/* Barbers Management */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Users className="w-8 h-8 text-green-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Barbers
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                Add new barbers, manage profiles, and configure their
                availability
              </p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
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
            <h1 className="text-6xl font-bold mb-6 text-gray-900">
              Welcome, {user.firstName}!
            </h1>
            <p className="text-xl text-gray-600 mb-8">Your barber dashboard</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Today's Schedule */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-blue-100 p-3 rounded-lg">
                  <Calendar className="w-8 h-8 text-blue-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Today's Schedule
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                View and manage your appointments for today
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                View Schedule
              </button>
            </div>

            {/* Availability */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Clock className="w-8 h-8 text-green-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Availability
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                Set your working hours and manage your schedule
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
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
                <h1 className="text-7xl font-bold mb-6 text-gray-900">TRiM</h1>
                <p className="text-2xl text-gray-600 mb-4">
                  Professional Barbershop Services
                </p>
                <p className="text-lg text-gray-500 mb-12 max-w-2xl mx-auto">
                  Book your appointment with us.
                </p>

                <button
                  onClick={() => navigate("/booking")}
                  className="bg-blue-600 text-white px-16 py-5 rounded-lg font-semibold hover:bg-blue-700 transition text-xl shadow-lg hover:shadow-xl"
                >
                  Book Your Appointment
                </button>

                {/* Features Section */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20">
                  <div className="bg-white rounded-xl shadow-lg p-8">
                    <div className="flex items-center justify-center mb-4">
                      <div className="bg-blue-100 p-4 rounded-lg">
                        <Scissors className="w-10 h-10 text-blue-600" />
                      </div>
                    </div>
                    <h3 className="text-xl font-bold mb-2">Expert Barbers</h3>
                    <p className="text-gray-600">
                      Skilled professionals dedicated to your perfect look
                    </p>
                  </div>
                  <div className="bg-white rounded-xl shadow-lg p-8">
                    <div className="flex items-center justify-center mb-4">
                      <div className="bg-green-100 p-4 rounded-lg">
                        <Calendar className="w-10 h-10 text-green-600" />
                      </div>
                    </div>
                    <h3 className="text-xl font-bold mb-2">Easy Booking</h3>
                    <p className="text-gray-600">
                      Book online anytime, manage appointments effortlessly
                    </p>
                  </div>
                  <div className="bg-white rounded-xl shadow-lg p-8">
                    <div className="flex items-center justify-center mb-4">
                      <div className="bg-yellow-100 p-4 rounded-lg">
                        <Star className="w-10 h-10 text-yellow-600" />
                      </div>
                    </div>
                    <h3 className="text-xl font-bold mb-2">Premium Service</h3>
                    <p className="text-gray-600">
                      Quality cuts and styling for every occasion
                    </p>
                  </div>
                </div>
              </div>
            ) : (
              // Logged-in Customer View
              <>
                <div className="text-center mb-16">
                  <h1 className="text-6xl font-bold mb-6 text-gray-900">
                    Welcome back, {user?.firstName}.
                  </h1>
                  <p className="text-xl text-gray-600 mb-8">
                    Ready for your next appointment?
                  </p>

                  <div className="flex gap-4 justify-center">
                    <button
                      onClick={() => navigate("/booking")}
                      className="bg-blue-600 text-white px-12 py-4 rounded-lg font-semibold hover:bg-blue-700 transition text-lg shadow-lg hover:shadow-xl"
                    >
                      Book New Appointment
                    </button>
                    <button
                      onClick={() => navigate("/my-bookings")}
                      className="bg-white border-2 border-blue-600 text-blue-600 px-12 py-4 rounded-lg font-semibold hover:bg-blue-50 transition text-lg shadow-lg hover:shadow-xl"
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
