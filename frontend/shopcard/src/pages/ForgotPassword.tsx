import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const ForgotPassword: React.FC = () => {
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotStatus, setForgotStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');



  const handleForgotPasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setForgotStatus('loading');
    try {
      const res = await axiosClient.post('/auth/forgot-password', null, {
        params: { email: forgotEmail }
      });
      setForgotStatus('success');
      setSuccess(res.data || 'If the email exists, a password reset link has been dispatched to your inbox.');
      setForgotEmail('');
    } catch (err: any) {
      setForgotStatus('error');
      setError(err.response?.data?.message || err.message || 'Failed to request password reset');
    }
  };

  return (
    <div className="min-h-screen flex bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-300 relative overflow-hidden">


      {/* LEFT SIDE: Brand Intro (Hidden on mobile) */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-tr from-indigo-600 via-indigo-700 to-purple-800 text-white flex-col justify-between p-16 relative">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        
        <div>
          <span className="px-3 py-1 bg-white/10 rounded-full text-xs font-bold tracking-widest uppercase text-indigo-200">
            Account Recovery
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight">
            Recover your<br />credentials.
          </h2>
          <p className="text-indigo-200 mt-4 text-lg font-medium max-w-md">
            Enter your registered email address and we will dispatch a secure link to reset your account password.
          </p>
        </div>

        {/* Informative recovery block */}
        <div className="bg-white/5 border border-white/10 rounded-2xl p-6 backdrop-blur-sm max-w-md">
          <h4 className="font-bold text-sm text-white uppercase tracking-wider mb-2">Password Safety</h4>
          <p className="text-xs text-indigo-200 leading-relaxed">
            All passwords on ShopCard are encrypted with high-entropy BCrypt hashes. Our support team can never view your current password.
          </p>
        </div>

        <div className="text-xs text-indigo-300 font-medium">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* RIGHT SIDE: Form Panel */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative">
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-slate-200 dark:border-slate-800 bg-white/70 dark:bg-slate-900/60 backdrop-blur-md">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-600 dark:from-indigo-400 dark:via-purple-400 dark:to-pink-400">
              Recover Pass
            </h1>
            <p className="text-slate-500 dark:text-slate-400 text-sm mt-2 font-medium">
              Reset Your Account Password
            </p>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-lg mb-4 text-center font-medium">
              {error}
            </div>
          )}
          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs px-4 py-3 rounded-lg mb-4 text-center font-medium">
              {success}
            </div>
          )}

          <form onSubmit={handleForgotPasswordSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Email Address</label>
              <input
                type="email"
                required
                disabled={forgotStatus === 'loading'}
                placeholder="your-email@example.com"
                className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                value={forgotEmail}
                onChange={(e) => setForgotEmail(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={forgotStatus === 'loading'}
              className="w-full py-3 bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 dark:from-indigo-500 dark:to-purple-600 dark:hover:from-indigo-600 dark:hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 flex justify-center items-center"
            >
              {forgotStatus === 'loading' ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                  Sending...
                </>
              ) : (
                'Send Reset Link'
              )}
            </button>

            <div className="text-center mt-6">
              <Link
                to="/login"
                className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors font-semibold"
              >
                Back to Sign In
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
