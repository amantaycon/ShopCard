import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Missing email verification token.');
      return;
    }

    const verify = async () => {
      try {
        const res = await axiosClient.get('/auth/verify', {
          params: { token }
        });
        setStatus('success');
        setMessage(res.data);
      } catch (err: any) {
        setStatus('error');
        setMessage(err.response?.data?.message || 'Email verification failed. The token may have expired or is invalid.');
      }
    };

    verify();
  }, [token]);

  return (
    <div className="min-h-screen flex bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-slate-950 dark:via-emerald-950/20 dark:to-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-400 relative overflow-hidden">
      {/* Decorative background blur blobs */}
      <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-emerald-200/20 dark:bg-emerald-950/15 rounded-full blur-3xl -z-10 animate-pulse"></div>
      <div className="absolute -bottom-40 -right-40 w-[500px] h-[500px] bg-teal-200/20 dark:bg-emerald-900/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

      <div className="w-full max-w-md mx-auto my-auto relative z-10 glass-panel rounded-2xl p-8 border border-emerald-100/50 dark:border-emerald-800/20 bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl shadow-[0_20px_50px_rgba(16,185,129,0.06)] dark:shadow-none text-center">
        <h1 className="text-3xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-emerald-600 via-teal-500 to-emerald-700 dark:from-emerald-400 dark:via-teal-300 dark:to-emerald-500 mb-6">
          Email Verification
        </h1>

        {status === 'loading' && (
          <div className="space-y-4">
            <div className="inline-block w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-slate-500 dark:text-slate-400 text-sm">Validating verification token with Identity Server...</p>
          </div>
        )}

        {status === 'success' && (
          <div className="space-y-6">
            <div className="w-16 h-16 bg-emerald-500/10 border border-emerald-500/30 rounded-full flex items-center justify-center mx-auto text-emerald-600 dark:text-emerald-400 text-2xl animate-bounce">
              ✓
            </div>
            <p className="text-emerald-900 dark:text-emerald-100 text-sm font-semibold">{message}</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-sm transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 hover:shadow-emerald-600/20 active:scale-98 flex justify-center items-center cursor-pointer"
            >
              Back to Login
            </button>
          </div>
        )}

        {status === 'error' && (
          <div className="space-y-6">
            <div className="w-16 h-16 bg-red-500/10 border border-red-500/30 rounded-full flex items-center justify-center mx-auto text-red-500 dark:text-red-400 text-2xl animate-pulse">
              ✗
            </div>
            <p className="text-red-500 dark:text-red-450 text-sm font-semibold">{message}</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-emerald-300 dark:hover:border-emerald-800 hover:bg-emerald-50/20 dark:hover:bg-emerald-950/20 text-slate-700 dark:text-white rounded-xl font-semibold text-sm transition-all cursor-pointer"
            >
              Return to Login
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;
