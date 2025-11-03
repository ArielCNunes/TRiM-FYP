import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { FaCalendarAlt, FaUsers, FaClock } from "react-icons/fa";

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      {/* Hero Section */}
      <div className="max-w-7xl mx-auto px-6 py-20">
        <div className="text-center mb-16">
          <h1 className="text-6xl font-bold mb-6 text-gray-900">TRiM</h1>

          {isAuthenticated ? (
            <button
              onClick={() => navigate("/booking")}
              className="bg-blue-600 text-white px-10 py-4 rounded-lg font-semibold hover:bg-blue-700 transition text-lg shadow-lg hover:shadow-xl"
            >
              Book an Appointment
            </button>
          ) : (
            <div className="flex gap-4 justify-center">
              <button
                onClick={() => navigate("/auth")}
                className="bg-blue-600 text-white px-10 py-4 rounded-lg font-semibold hover:bg-blue-700 transition shadow-lg hover:shadow-xl"
              >
                Sign In
              </button>
              <button
                onClick={() => navigate("/auth")}
                className="bg-white text-blue-600 border-2 border-blue-600 px-10 py-4 rounded-lg font-semibold hover:bg-blue-50 transition"
              >
                Sign Up
              </button>
            </div>
          )}
        </div>

        {/* Features Section */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20">
          <div className="text-center p-8 bg-white rounded-xl shadow-md hover:shadow-lg transition">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaCalendarAlt className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-gray-900">
              Easy Booking
            </h3>
            <p className="text-gray-600">
              Choose your preferred barber, service, and time slot in just a few
              clicks.
            </p>
          </div>

          <div className="text-center p-8 bg-white rounded-xl shadow-md hover:shadow-lg transition">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaUsers className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-gray-900">
              Expert Barbers
            </h3>
            <p className="text-gray-600">
              Our skilled professionals are dedicated to giving you the perfect
              cut every time.
            </p>
          </div>

          <div className="text-center p-8 bg-white rounded-xl shadow-md hover:shadow-lg transition">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaClock className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-gray-900">
              Flexible Hours
            </h3>
            <p className="text-gray-600">
              Find appointment slots that fit your schedule with our extended
              operating hours.
            </p>
          </div>
        </div>

        {/* How It Works Section */}
        <div className="mt-24 text-center">
          <h2 className="text-4xl font-bold mb-12 text-gray-900">
            How It Works
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="relative">
              <div className="w-12 h-12 bg-blue-600 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                1
              </div>
              <h4 className="font-semibold text-lg mb-2 text-gray-900">
                Choose Service
              </h4>
              <p className="text-gray-600">
                Select from our range of premium services
              </p>
            </div>
            <div className="relative">
              <div className="w-12 h-12 bg-blue-600 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                2
              </div>
              <h4 className="font-semibold text-lg mb-2 text-gray-900">
                Pick Your Barber
              </h4>
              <p className="text-gray-600">Choose from our talented team</p>
            </div>
            <div className="relative">
              <div className="w-12 h-12 bg-blue-600 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                3
              </div>
              <h4 className="font-semibold text-lg mb-2 text-gray-900">
                Select Time
              </h4>
              <p className="text-gray-600">Find a time that works for you</p>
            </div>
            <div className="relative">
              <div className="w-12 h-12 bg-blue-600 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                4
              </div>
              <h4 className="font-semibold text-lg mb-2 text-gray-900">
                Confirm & Pay
              </h4>
              <p className="text-gray-600">Secure your appointment with ease</p>
            </div>
          </div>
        </div>

        {/* CTA Section */}
        {!isAuthenticated && (
          <div className="mt-24 bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-12 text-center text-white shadow-xl">
            <h2 className="text-3xl font-bold mb-4">
              Ready to Look Your Best?
            </h2>
            <p className="text-xl mb-8 text-blue-100">
              Join hundreds of satisfied customers and book your appointment
              today.
            </p>
            <button
              onClick={() => navigate("/auth")}
              className="bg-white text-blue-600 px-10 py-4 rounded-lg font-semibold hover:bg-blue-50 transition shadow-lg text-lg"
            >
              Get Started Now
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
