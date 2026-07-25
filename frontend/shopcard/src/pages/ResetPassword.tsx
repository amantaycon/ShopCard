import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const ResetPassword: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      setStatus('error');
      setMessage('Missing or invalid reset token. Please request another password reset link.');
      return;
    }

    if (newPassword.length < 6) {
      setStatus('error');
      setMessage('Password must be at least 6 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setStatus('error');
      setMessage('Passwords do not match.');
      return;
    }

    setStatus('loading');
    setMessage('');

    try {
      const res = await axiosClient.post('/auth/reset-password', {
        token,
        newPassword,
      });
      setStatus('success');
      setMessage(res.data || 'Password has been reset successfully!');
    } catch (err: any) {
      setStatus('error');
      setMessage(err.response?.data?.message || 'Failed to reset password. The token may have expired or is invalid.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 flex flex-col justify-center items-center px-4 relative overflow-hidden">
      {/* Background glowing blobs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-violet-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

      <div className="w-full max-w-md glass-panel rounded-2xl shadow-2xl p-8 border border-slate-800">
        <h1 className="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 mb-6 text-center">
          Reset Password
        </h1>

        {status === 'success' ? (
          <div className="space-y-6 text-center">
            <div className="w-16 h-16 bg-emerald-500/10 border border-emerald-500/30 rounded-full flex items-center justify-center mx-auto text-emerald-400 text-2xl animate-bounce">
              ✓
            </div>
            <p className="text-slate-200 text-sm font-semibold">{message}</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 glow-btn"
            >
              Go to Login
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {message && (
              <div className={`text-xs px-4 py-3 rounded-lg text-center ${
                status === 'error' ? 'bg-red-500/10 border border-red-500/20 text-red-400' : 'text-slate-400'
              }`}>
                {message}
              </div>
            )}

            {!token && (
              <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs px-4 py-3 rounded-lg text-center">
                Warning: No reset token detected in the URL.
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">New Password</label>
              <input
                type="password"
                required
                disabled={status === 'loading'}
                className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 text-white transition-colors"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="••••••••"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Confirm Password</label>
              <input
                type="password"
                required
                disabled={status === 'loading'}
                className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 text-white transition-colors"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={status === 'loading'}
              className="w-full py-3 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg shadow-indigo-500/20 active:scale-98 glow-btn flex justify-center items-center"
            >
              {status === 'loading' ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                  Resetting Password...
                </>
              ) : (
                'Reset Password'
              )}
            </button>

            <div className="text-center mt-4">
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="text-xs text-slate-400 hover:text-slate-300 transition-colors"
              >
                Back to Login
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;
