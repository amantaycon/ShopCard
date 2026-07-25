import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

interface ProfileData {
  userId: string;
  username: string;
  firstName: string;
  lastName: string;
  bio: string;
  avatarUrl: string;
}

const UserProfileView: React.FC = () => {
  const { username } = useParams<{ username: string }>();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!username) return;

    axiosClient.get(`/profiles/u/${username}`)
      .then(res => {
        setProfile(res.data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.response?.status === 404 
          ? 'User profile not found.' 
          : 'Failed to retrieve profile details.');
        setLoading(false);
      });
  }, [username]);

  if (loading) {
    return (
      <div className="min-h-screen flex bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-slate-950 dark:via-emerald-950/20 dark:to-slate-950 items-center justify-center text-brand-text">
        <div className="flex flex-col items-center gap-2">
          <div className="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-xs font-semibold">Retrieving Profile...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-slate-950 dark:via-emerald-950/20 dark:to-slate-950 text-slate-800 dark:text-slate-100 flex flex-col justify-center items-center py-12 px-4 relative overflow-hidden">
      {/* Background Glowing Blobs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-emerald-500/5 rounded-full blur-3xl -z-10 animate-pulse"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-teal-500/5 rounded-full blur-3xl -z-10 animate-pulse"></div>

      <div className="w-full max-w-lg glass-panel rounded-3xl overflow-hidden shadow-2xl relative border border-emerald-100/50 dark:border-emerald-800/25 bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl">
        {/* Colorful Gradient Header Banner */}
        <div className="h-32 bg-gradient-to-r from-emerald-400 via-teal-500 to-emerald-600 relative">
          <button 
            onClick={() => navigate('/')} 
            className="absolute top-4 left-4 bg-white/20 hover:bg-white/30 text-white text-xs font-bold px-3 py-1.5 rounded-full backdrop-blur-md transition-all active:scale-95 cursor-pointer"
          >
            ← Back Home
          </button>
        </div>

        {/* Profile Content */}
        <div className="px-8 pb-8 pt-0 relative flex flex-col items-center">
          {/* Avatar Positioned Over Banner */}
          <div className="relative -mt-16 mb-4">
            {profile?.avatarUrl ? (
              <img 
                src={profile.avatarUrl} 
                alt={profile.username} 
                className="w-28 h-28 rounded-full border-4 border-brand-card shadow-lg object-cover bg-white"
              />
            ) : (
              <div className="w-28 h-28 rounded-full border-4 border-brand-card shadow-lg bg-gradient-to-tr from-emerald-500 to-teal-500 flex items-center justify-center text-white text-3xl font-black uppercase">
                {profile?.firstName ? profile.firstName.charAt(0) : 'U'}
              </div>
            )}
            <span className="absolute bottom-1.5 right-1.5 bg-emerald-500 w-5 h-5 rounded-full border-2 border-brand-card shadow-md flex items-center justify-center text-[10px] text-white font-bold" title="Online Verified">
              ✓
            </span>
          </div>

          {error ? (
            <div className="text-center py-6">
              <div className="text-red-500 text-xl font-bold mb-2">Oops!</div>
              <p className="text-brand-text-muted text-sm">{error}</p>
            </div>
          ) : (
            <div className="w-full text-center space-y-4">
              <div>
                <h2 className="text-2xl font-black tracking-tight text-brand-text">
                  {profile?.firstName} {profile?.lastName}
                </h2>
                <p className="text-emerald-600 dark:text-emerald-400 text-sm font-semibold mt-0.5">
                  @{profile?.username}
                </p>
              </div>

              {/* Bio Section */}
              <div className="p-5 rounded-2xl bg-brand-bg/50 border border-brand-border/40 text-left relative">
                <span className="text-3xl text-emerald-500/20 absolute top-2 left-3 font-serif">“</span>
                <h4 className="text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-2 relative pl-4">About Me</h4>
                <p className="text-xs text-brand-text-muted leading-relaxed font-medium relative pl-4 pr-2 whitespace-pre-line">
                  {profile?.bio || "This user hasn't written a biography yet. Stay tuned!"}
                </p>
              </div>

              {/* Features / Roles Badges */}
              <div className="flex justify-center gap-2 pt-2">
                <span className="px-3 py-1 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-[10px] font-bold uppercase rounded-full tracking-wider border border-emerald-500/20">
                  Verified Member
                </span>
                <span className="px-3 py-1 bg-teal-500/10 text-teal-600 dark:text-teal-400 text-[10px] font-bold uppercase rounded-full tracking-wider border border-teal-500/20">
                  ShopCard Net
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfileView;
