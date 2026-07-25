import React, { useState, useEffect } from 'react';
import { useTheme } from '../context/ThemeContext';
import { useAppDispatch } from '../store';
import { updateUser } from '../store/authSlice';
import axiosClient from '../api/axiosClient';

interface SettingsPanelProps {
  onClose?: () => void;
}

const SettingsPanel: React.FC<SettingsPanelProps> = ({ onClose }) => {
  const { theme, setTheme, availableThemes } = useTheme();
  const dispatch = useAppDispatch();

  // Profile fields state
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [username, setUsername] = useState('');
  const [bio, setBio] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [emailNotifications, setEmailNotifications] = useState(true);

  // Status indicators
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch current user settings on mount
  useEffect(() => {
    axiosClient.get('/profiles/my')
      .then(res => {
        if (res.data) {
          setFirstName(res.data.firstName || '');
          setLastName(res.data.lastName || '');
          setUsername(res.data.username || '');
          setBio(res.data.bio || '');
          setAvatarUrl(res.data.avatarUrl || '');
          setEmailNotifications(res.data.emailNotifications);
        }
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to fetch profile settings.');
        setLoading(false);
      });
  }, []);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      // 1. Save profile details
      await axiosClient.put('/profiles/my', {
        firstName,
        lastName,
        username,
        bio,
        avatarUrl
      });

      // 2. Save notification settings
      await axiosClient.put('/profiles/my/settings', {
        emailNotifications
      });

      // Sync with global auth state
      dispatch(updateUser({ firstName, lastName }));
      setSuccess('Settings updated successfully!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.response?.data || err.message || 'Failed to update profile settings.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="p-8 text-center text-brand-text">
        <div className="inline-block w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mb-2"></div>
        <div className="text-xs font-semibold text-brand-text-muted">Loading preferences...</div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-xl mx-auto space-y-6">
      {/* Settings Panel Header */}
      <div className="flex justify-between items-center pb-4 border-b border-brand-border">
        <div>
          <h2 className="text-lg font-black tracking-tight text-brand-text">Account Settings</h2>
          <p className="text-xs text-brand-text-muted">Manage theme colors and public profile details</p>
        </div>
        {onClose && (
          <button 
            onClick={onClose}
            className="text-xs font-bold px-3 py-1.5 rounded-lg border border-brand-border hover:bg-brand-bg/40 text-brand-text transition-all active:scale-95 cursor-pointer"
          >
            Close Settings
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-xl text-center font-semibold">
          {error}
        </div>
      )}
      {success && (
        <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs px-4 py-3 rounded-xl text-center font-semibold">
          {success}
        </div>
      )}

      {/* SECTION 1: THEME SELECTION */}
      <div className="space-y-3">
        <h3 className="text-xs font-bold text-brand-text uppercase tracking-wider">Appearance Themes</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {availableThemes.map(t => (
            <div
              key={t.id}
              onClick={() => setTheme(t.id)}
              className={`p-3 rounded-xl border text-center cursor-pointer transition-all duration-350 active:scale-95 ${
                theme === t.id
                  ? 'border-indigo-500 bg-indigo-500/5 ring-2 ring-indigo-500/10'
                  : 'border-brand-border bg-brand-card hover:border-brand-text-muted/40'
              }`}
            >
              <div className="text-xl mb-1">{t.icon}</div>
              <div className="text-[11px] font-bold text-brand-text">{t.name}</div>
            </div>
          ))}
        </div>
      </div>

      {/* SECTION 2: PROFILE & BIO FORM */}
      <form onSubmit={handleSaveProfile} className="space-y-4">
        <h3 className="text-xs font-bold text-brand-text uppercase tracking-wider pt-2">Profile Information</h3>
        
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">First Name</label>
            <input
              type="text"
              required
              className="w-full px-3.5 py-2 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-indigo-500 transition-colors"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Last Name</label>
            <input
              type="text"
              required
              className="w-full px-3.5 py-2 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-indigo-500 transition-colors"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </div>
        </div>

        <div>
          <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Username</label>
          <div className="relative">
            <span className="absolute left-3.5 top-2 text-xs text-indigo-500/40 font-bold">@</span>
            <input
              type="text"
              required
              className="w-full pl-7 pr-3.5 py-2 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-indigo-500 transition-colors font-semibold"
              value={username}
              onChange={(e) => setUsername(e.target.value.toLowerCase().replace(/[^a-z0-9_]/g, ''))}
            />
          </div>
          <p className="text-[9px] text-brand-text-muted mt-1">Public profile URL: /u/{username || 'username'}</p>
        </div>

        <div>
          <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Biography</label>
          <textarea
            maxLength={1000}
            rows={3}
            placeholder="Tell us something about yourself..."
            className="w-full px-3.5 py-2 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-indigo-500 transition-colors resize-none"
            value={bio}
            onChange={(e) => setBio(e.target.value)}
          />
        </div>

        <div>
          <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Avatar Image URL</label>
          <input
            type="text"
            placeholder="https://example.com/photo.png"
            className="w-full px-3.5 py-2 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-indigo-500 transition-colors"
            value={avatarUrl}
            onChange={(e) => setAvatarUrl(e.target.value)}
          />
        </div>

        {/* SECTION 3: NOTIFICATION SETTINGS */}
        <div className="flex items-center justify-between p-3 rounded-xl border border-brand-border bg-brand-card">
          <div>
            <h4 className="text-[11px] font-bold text-brand-text">Email Notifications</h4>
            <p className="text-[9px] text-brand-text-muted">Receive click &amp; collect updates via mail</p>
          </div>
          <input
            type="checkbox"
            className="w-4 h-4 text-indigo-600 border-brand-input-border rounded focus:ring-indigo-500"
            checked={emailNotifications}
            onChange={(e) => setEmailNotifications(e.target.checked)}
          />
        </div>

        <button
          type="submit"
          disabled={saving}
          className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-semibold text-xs transition-all shadow-md active:scale-98 flex justify-center items-center cursor-pointer"
        >
          {saving ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          ) : (
            'Save Changes'
          )}
        </button>
      </form>
    </div>
  );
};

export default SettingsPanel;
