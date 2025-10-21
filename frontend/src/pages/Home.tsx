import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../store/hooks';

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector(state => state.auth.isAuthenticated);

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      {/* Hero Section */}
      <div className="max-w-6xl mx-auto px-6 py-20 text-center">
        <h1 className="text-5xl font-bold mb-4 text-gray-900">
          TRiM
        </h1>

        {isAuthenticated ? (
          <button
            onClick={() => navigate('/booking')}
            className="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition text-lg"
          >
            Book an Appointment
          </button>
        ) : (
          <div className="flex gap-4 justify-center">
            <button
              onClick={() => navigate('/login')}
              className="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
            >
              Sign In
            </button>
            <button
              onClick={() => navigate('/register')}
              className="bg-gray-200 text-gray-900 px-8 py-3 rounded-lg font-semibold hover:bg-gray-300 transition"
            >
              Sign Up
            </button>
          </div>
        )}
      </div>
    </div>
  );
}