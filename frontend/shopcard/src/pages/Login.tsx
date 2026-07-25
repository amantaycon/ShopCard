import React, { useState, useEffect } from 'react';
import { useAppDispatch } from '../store';
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
  const [showPassword, setShowPassword] = useState(false);

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
    <div className="min-h-screen flex bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-slate-950 dark:via-emerald-950/20 dark:to-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-400 relative overflow-hidden">
      {/* Decorative background blur blobs */}
      <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-emerald-200/20 dark:bg-emerald-950/15 rounded-full blur-3xl -z-10 animate-pulse"></div>
      <div className="absolute -bottom-40 -right-40 w-[500px] h-[500px] bg-teal-200/20 dark:bg-emerald-900/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

      {/* LEFT SIDE: Brand Intro (Hidden on mobile) */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-tr from-emerald-50 via-emerald-100/30 to-teal-50 dark:from-emerald-950/40 dark:via-emerald-900/20 dark:to-teal-950/40 text-emerald-950 dark:text-emerald-50 flex-col justify-between p-16 relative">
        {/* Background micro grid */}
        <div className="absolute inset-0 opacity-[0.03] dark:opacity-[0.06] bg-[radial-gradient(#10b981_1px,transparent_1px)] [background-size:20px_20px]"></div>
        
        <div className="relative z-10">
          <span className="px-3.5 py-1 bg-emerald-100/70 dark:bg-emerald-900/30 border border-emerald-200/30 dark:border-emerald-800/30 rounded-full text-xs font-bold tracking-widest uppercase text-emerald-700 dark:text-emerald-300">
            Next-Gen Commerce
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight bg-gradient-to-br from-emerald-950 via-emerald-900 to-teal-950 dark:from-white dark:to-emerald-300 bg-clip-text text-transparent">
            ShopCard storefronts,<br />closer than ever.
          </h2>
          <p className="text-emerald-800/80 dark:text-emerald-200/80 mt-4 text-lg font-medium max-w-md leading-relaxed">
            The hyper-local Click &amp; Collect network connecting merchants, customers, and delivery partners in real-time.
          </p>
        </div>

        {/* Feature Grid */}
        <div className="grid grid-cols-2 gap-8 my-12 relative z-10">
          <div className="space-y-3 group">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center text-emerald-600 dark:text-emerald-400 group-hover:scale-110 transition-transform duration-300">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <h4 className="font-bold text-emerald-950 dark:text-emerald-100">Click &amp; Collect</h4>
            <p className="text-xs text-emerald-800/70 dark:text-emerald-300/70 leading-relaxed">Reserve items online and pick up at the counter instantly.</p>
          </div>
          <div className="space-y-3 group">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center text-emerald-600 dark:text-emerald-400 group-hover:scale-110 transition-transform duration-300">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <h4 className="font-bold text-emerald-950 dark:text-emerald-100">Geospatial PostGIS</h4>
            <p className="text-xs text-emerald-800/70 dark:text-emerald-300/70 leading-relaxed">Interactive maps to locate catalog stock within meter accuracy.</p>
          </div>
          <div className="space-y-3 group">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center text-emerald-600 dark:text-emerald-400 group-hover:scale-110 transition-transform duration-300">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <h4 className="font-bold text-emerald-950 dark:text-emerald-100">Secure OTP Pickup</h4>
            <p className="text-xs text-emerald-800/70 dark:text-emerald-300/70 leading-relaxed">Failsafe 6-digit tokens protect order preparation &amp; handovers.</p>
          </div>
          <div className="space-y-3 group">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center text-emerald-600 dark:text-emerald-400 group-hover:scale-110 transition-transform duration-300">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <h4 className="font-bold text-emerald-950 dark:text-emerald-100">Direct STOMP Chats</h4>
            <p className="text-xs text-emerald-800/70 dark:text-emerald-300/70 leading-relaxed">Real-time WebSockets keep users and storefronts in constant sync.</p>
          </div>
        </div>

        <div className="text-xs text-emerald-800/40 dark:text-emerald-400/40 font-semibold relative z-10">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* Background/Vertical Divider with Gradient */}
      <div className="hidden lg:block w-[1.5px] bg-gradient-to-b from-emerald-200 via-teal-400 to-emerald-500 dark:from-emerald-950 dark:via-teal-850 dark:to-emerald-950 opacity-60 self-stretch z-10 relative"></div>

      {/* RIGHT SIDE: Auth Panels */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative z-10">
        {/* Glow blob */}
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-emerald-500/5 dark:bg-emerald-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-emerald-100/50 dark:border-emerald-800/20 bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl shadow-[0_20px_50px_rgba(16,185,129,0.06)] dark:shadow-none">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-emerald-600 via-teal-500 to-emerald-700 dark:from-emerald-400 dark:via-teal-300 dark:to-emerald-500">
              ShopCard
            </h1>
            <p className="text-emerald-800/70 dark:text-emerald-400/60 text-sm mt-2 font-medium">
              Production-grade Click &amp; Collect Network
            </p>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-xl mb-4 text-center font-medium">
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
                  className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                  value={mockGoogleEmail}
                  onChange={(e) => setMockGoogleEmail(e.target.value)}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-sm transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 hover:shadow-emerald-600/20 active:scale-98 flex justify-center items-center cursor-pointer"
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
                  className="text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors font-semibold"
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
                  className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Password</label>
                  <Link
                    to="/forgot-password"
                    className="text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 transition-colors font-semibold"
                  >
                    Forgot Password?
                  </Link>
                </div>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    placeholder="••••••••"
                    className="w-full pl-4 pr-10 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-emerald-500 transition-colors cursor-pointer"
                  >
                    {showPassword ? (
                      <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    ) : (
                      <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-sm transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 hover:shadow-emerald-600/20 active:scale-98 flex justify-center items-center cursor-pointer"
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
                  <span className="bg-slate-50/50 dark:bg-emerald-950/20 px-2 text-slate-500 dark:text-emerald-400">Or Continue With</span>
                </div>
              </div>

              <button
                onClick={() => setShowGoogleMock(true)}
                className="w-full py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-emerald-300 dark:hover:border-emerald-800 hover:bg-emerald-50/20 dark:hover:bg-emerald-950/20 text-slate-700 dark:text-white rounded-xl font-semibold text-sm transition-all flex items-center justify-center gap-2 mb-4 shadow-sm cursor-pointer"
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
                  className="text-xs text-emerald-600 dark:text-emerald-400 hover:underline transition-colors font-semibold"
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
