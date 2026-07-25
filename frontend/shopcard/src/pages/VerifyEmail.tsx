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
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 flex flex-col justify-center items-center px-4 relative overflow-hidden">
      {/* Background glowing blobs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-violet-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

      <div className="w-full max-w-md glass-panel rounded-2xl shadow-2xl p-8 border border-slate-800 text-center">
        <h1 className="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-purple-400 mb-6">
          Email Verification
        </h1>

        {status === 'loading' && (
          <div className="space-y-4">
            <div className="inline-block w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-slate-400 text-sm">Validating verification token with Identity Server...</p>
          </div>
        )}

        {status === 'success' && (
          <div className="space-y-6">
            <div className="w-16 h-16 bg-emerald-500/10 border border-emerald-500/30 rounded-full flex items-center justify-center mx-auto text-emerald-400 text-2xl animate-bounce">
              ✓
            </div>
            <p className="text-slate-200 text-sm font-semibold">{message}</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 glow-btn"
            >
              Back to Login
            </button>
          </div>
        )}

        {status === 'error' && (
          <div className="space-y-6">
            <div className="w-16 h-16 bg-red-500/10 border border-red-500/30 rounded-full flex items-center justify-center mx-auto text-red-400 text-2xl animate-pulse">
              ✗
            </div>
            <p className="text-red-400 text-sm font-semibold">{message}</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-slate-900 border border-slate-850 hover:bg-slate-800 text-white rounded-lg font-semibold text-sm transition-all"
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
