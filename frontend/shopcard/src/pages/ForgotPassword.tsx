import React, { useState } from 'react';
import { Link } from 'react-router-dom';
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
            Account Recovery
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight bg-gradient-to-br from-emerald-950 via-emerald-900 to-teal-950 dark:from-white dark:to-emerald-300 bg-clip-text text-transparent">
            Recover your<br />credentials.
          </h2>
          <p className="text-emerald-800/80 dark:text-emerald-200/80 mt-4 text-lg font-medium max-w-md leading-relaxed">
            Enter your registered email address and we will dispatch a secure link to reset your account password.
          </p>
        </div>

        {/* Informative recovery block */}
        <div className="bg-white/40 dark:bg-emerald-950/20 border border-emerald-200/30 dark:border-emerald-800/20 rounded-2xl p-6 backdrop-blur-md max-w-md relative z-10">
          <h4 className="font-bold text-sm text-emerald-900 dark:text-emerald-100 uppercase tracking-wider mb-2">Password Safety</h4>
          <p className="text-xs text-emerald-800/85 dark:text-emerald-300/80 leading-relaxed font-medium">
            All passwords on ShopCard are encrypted with high-entropy BCrypt hashes. Our support team can never view your current password.
          </p>
        </div>

        <div className="text-xs text-emerald-800/40 dark:text-emerald-400/40 font-semibold relative z-10">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* Background/Vertical Divider with Gradient */}
      <div className="hidden lg:block w-[1.5px] bg-gradient-to-b from-emerald-200 via-teal-400 to-emerald-500 dark:from-emerald-950 dark:via-teal-850 dark:to-emerald-950 opacity-60 self-stretch z-10 relative"></div>

      {/* RIGHT SIDE: Form Panel */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative z-10">
        {/* Glow blob */}
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-emerald-500/5 dark:bg-emerald-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-emerald-100/50 dark:border-emerald-800/20 bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl shadow-[0_20px_50px_rgba(16,185,129,0.06)] dark:shadow-none">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-emerald-600 via-teal-500 to-emerald-700 dark:from-emerald-400 dark:via-teal-300 dark:to-emerald-500">
              Recover Pass
            </h1>
            <p className="text-emerald-800/70 dark:text-emerald-400/60 text-sm mt-2 font-medium">
              Reset Your Account Password
            </p>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-xl mb-4 text-center font-medium">
              {error}
            </div>
          )}
          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs px-4 py-3 rounded-xl mb-4 text-center font-medium">
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
                className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                value={forgotEmail}
                onChange={(e) => setForgotEmail(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={forgotStatus === 'loading'}
              className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-sm transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 hover:shadow-emerald-600/20 active:scale-98 flex justify-center items-center cursor-pointer"
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
                className="text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors font-semibold"
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
