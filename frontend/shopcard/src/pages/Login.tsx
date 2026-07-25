import React, { useState, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../store';
import { loginThunk, loginGoogleThunk, clearError } from '../store/authSlice';
import { useNavigate, Link } from 'react-router-dom';

const Login: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  // Error/Success alerts
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Sign In fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Google Mock Login fields
  const [mockGoogleEmail, setMockGoogleEmail] = useState('');
  const [showGoogleMock, setShowGoogleMock] = useState(false);

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const handleSignInSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await dispatch(loginThunk({ email, password })).unwrap();
      navigate('/');
    } catch (err: any) {
      setError(err || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleMockLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (!mockGoogleEmail.includes('@')) {
        setError('Please enter a valid email');
        setLoading(false);
        return;
      }
      await dispatch(loginGoogleThunk(mockGoogleEmail)).unwrap();
      navigate('/');
    } catch (err: any) {
      setError(err || 'Google Auth simulation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-300 relative overflow-hidden">


      {/* LEFT SIDE: Brand Intro (Hidden on mobile) */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-tr from-indigo-600 via-indigo-700 to-purple-800 text-white flex-col justify-between p-16 relative">
        {/* Background micro grid */}
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        
        <div>
          <span className="px-3 py-1 bg-white/10 rounded-full text-xs font-bold tracking-widest uppercase text-indigo-200">
            Next-Gen Commerce
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight">
            ShopCard storefronts,<br />closer than ever.
          </h2>
          <p className="text-indigo-200 mt-4 text-lg font-medium max-w-md">
            The hyper-local Click &amp; Collect network connecting merchants, customers, and delivery partners in real-time.
          </p>
        </div>

        {/* Feature Grid */}
        <div className="grid grid-cols-2 gap-8 my-12">
          <div className="space-y-2">
            <div className="text-2xl">🏪</div>
            <h4 className="font-bold text-white">Click &amp; Collect</h4>
            <p className="text-xs text-indigo-200">Reserve items online and pick up at the counter instantly.</p>
          </div>
          <div className="space-y-2">
            <div className="text-2xl">📍</div>
            <h4 className="font-bold text-white">Geospatial PostGIS</h4>
            <p className="text-xs text-indigo-200">Interactive maps to locate catalog stock within meter accuracy.</p>
          </div>
          <div className="space-y-2">
            <div className="text-2xl">🔒</div>
            <h4 className="font-bold text-white">Secure OTP Pickup</h4>
            <p className="text-xs text-indigo-200">Failsafe 6-digit tokens protect order preparation &amp; handovers.</p>
          </div>
          <div className="space-y-2">
            <div className="text-2xl">💬</div>
            <h4 className="font-bold text-white">Direct STOMP Chats</h4>
            <p className="text-xs text-indigo-200">Real-time WebSockets keep users and storefronts in constant sync.</p>
          </div>
        </div>

        <div className="text-xs text-indigo-300 font-medium">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* RIGHT SIDE: Auth Panels */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative">
        {/* Glow blobs on right side (visible in dark mode) */}
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-slate-200 dark:border-slate-800 bg-white/70 dark:bg-slate-900/60 backdrop-blur-md">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-600 dark:from-indigo-400 dark:via-purple-400 dark:to-pink-400">
              ShopCard
            </h1>
            <p className="text-slate-500 dark:text-slate-400 text-sm mt-2 font-medium">
              Production-grade Click &amp; Collect Network
            </p>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-lg mb-4 text-center font-medium">
              {error}
            </div>
          )}

          {showGoogleMock ? (
            <form onSubmit={handleGoogleMockLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Google Email to Simulate</label>
                <input
                  type="email"
                  required
                  placeholder="user@gmail.com"
                  className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                  value={mockGoogleEmail}
                  onChange={(e) => setMockGoogleEmail(e.target.value)}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 flex justify-center items-center"
              >
                {loading ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  'Simulate Google Sign In'
                )}
              </button>

              <div className="text-center mt-6">
                <button
                  type="button"
                  onClick={() => setShowGoogleMock(false)}
                  className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors font-semibold"
                >
                  Cancel Mock Sign In
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleSignInSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  placeholder="name@example.com"
                  className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Password</label>
                  <Link
                    to="/forgot-password"
                    className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors font-semibold"
                  >
                    Forgot Password?
                  </Link>
                </div>
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 dark:from-indigo-500 dark:to-purple-600 dark:hover:from-indigo-600 dark:hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 flex justify-center items-center"
              >
                {loading ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>
          )}

          {!showGoogleMock && (
            <>
              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-slate-200 dark:border-slate-800"></div>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-slate-50 dark:bg-slate-900 px-2 text-slate-500">Or Continue With</span>
                </div>
              </div>

              <button
                onClick={() => setShowGoogleMock(true)}
                className="w-full py-2.5 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-white rounded-lg font-medium text-sm transition-all flex items-center justify-center gap-2 mb-4 shadow-sm"
              >
                {/* Google Icon */}
                <svg className="w-4 h-4" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v3.9h6.6c-.28 1.5-1.11 2.76-2.39 3.62v3h3.86c2.26-2.09 3.67-5.17 3.67-8.45z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.86-3c-1.08.72-2.45 1.16-4.07 1.16-3.11 0-5.74-2.11-6.68-4.96H1.21v3.15C3.18 21.88 7.31 24 12 24z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.32 14.29a7.16 7.16 0 0 1 0-4.58V6.56H1.21a11.94 11.94 0 0 0 0 10.88l4.11-3.15z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.18 2.12 1.21 5.56l4.11 3.15c.94-2.85 3.57-4.96 6.68-4.96z"
                  />
                </svg>
                Sign in with Google
              </button>

              <div className="text-center mt-6">
                <Link
                  to="/register"
                  className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors font-semibold"
                >
                  Don't have an account? Sign Up
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;
