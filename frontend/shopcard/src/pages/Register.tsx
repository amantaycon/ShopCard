import React, { useState, useEffect } from 'react';
import { useAppDispatch } from '../store';
import { initiateRegisterThunk, updateUser } from '../store/authSlice';
import { useNavigate, Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const Register: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [regStep, setRegStep] = useState(1);

  // Status alerts
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Step 1 Registration fields
  const [regEmail, setRegEmail] = useState('');
  const [code, setCode] = useState('');
  const [codeSent, setCodeSent] = useState(false);
  const [codeVerified, setCodeVerified] = useState(false);
  const [regPassword, setRegPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Step 2 Profile fields
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [username, setUsername] = useState('');
  const [agreedTerms, setAgreedTerms] = useState(false);



  const handleSendCode = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await axiosClient.post('/auth/register/send-code', { email: regEmail });
      setCodeSent(true);
      setSuccess('Verification code sent! Please check your email inbox.');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to send verification code');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyCode = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await axiosClient.post('/auth/register/verify-code', { email: regEmail, code });
      setCodeVerified(true);
      setSuccess('Email successfully verified! Enter a password to secure your account.');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Invalid or expired verification code');
    } finally {
      setLoading(false);
    }
  };

  const handleInitiate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (regPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      await dispatch(initiateRegisterThunk({ email: regEmail, password: regPassword })).unwrap();
      setSuccess('Verification success! Let\'s setup your profile.');
      setRegStep(2);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to initiate registration');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (!agreedTerms) {
      setError('You must agree to the terms and conditions');
      return;
    }
    setLoading(true);
    try {
      await axiosClient.put('/auth/profile', {
        firstName,
        lastName,
        dateOfBirth,
        username,
        agreedTerms
      });

      // Synchronize with the new profile-service database
      try {
        await axiosClient.put('/profiles/my', {
          firstName,
          lastName,
          username
        });
      } catch (profileErr) {
        console.warn("Failed to initialize profile details in profile-service:", profileErr);
      }

      dispatch(updateUser({ firstName, lastName }));
      setSuccess('Profile details saved! Redirecting to choose your role...');
      setTimeout(() => {
        navigate('/onboarding');
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const renderStepsIndicator = () => {
    const steps = [
      { num: 1, label: 'Verify' },
      { num: 2, label: 'Profile' }
    ];

    return (
      <div className="flex justify-center items-center mb-8 px-2 max-w-xs mx-auto">
        {steps.map((step, idx) => (
          <React.Fragment key={step.num}>
            {idx > 0 && (
              <div className={`w-16 h-0.5 mx-2 ${regStep >= step.num ? 'bg-indigo-500' : 'bg-slate-200 dark:bg-slate-800'}`} />
            )}
            <div className="flex flex-col items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-300 ${
                regStep === step.num
                  ? 'bg-indigo-600 dark:bg-indigo-500 text-white ring-4 ring-indigo-500/20 scale-110'
                  : regStep > step.num
                  ? 'bg-emerald-500 text-white'
                  : 'bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-800 text-slate-400'
              }`}>
                {regStep > step.num ? '✓' : step.num}
              </div>
              <span className={`text-[10px] mt-1 font-semibold uppercase tracking-wider ${
                regStep === step.num ? 'text-indigo-600 dark:text-indigo-400' : regStep > step.num ? 'text-emerald-500' : 'text-slate-400'
              }`}>
                {step.label}
              </span>
            </div>
          </React.Fragment>
        ))}
      </div>
    );
  };

  return (
    <div className="min-h-screen flex bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-300 relative overflow-hidden">


      {/* LEFT SIDE: Brand Intro (Hidden on mobile) */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-tr from-indigo-600 via-indigo-700 to-purple-800 text-white flex-col justify-between p-16 relative">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        
        <div>
          <span className="px-3 py-1 bg-white/10 rounded-full text-xs font-bold tracking-widest uppercase text-indigo-200">
            Secure Registration
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight">
            Join the local<br />neighborhood net.
          </h2>
          <p className="text-indigo-200 mt-4 text-lg font-medium max-w-md">
            Verify your email and complete your personal profile to start using the ShopCard click &amp; collect app.
          </p>
        </div>

        {/* Informative Step Box */}
        <div className="bg-white/5 border border-white/10 rounded-2xl p-6 backdrop-blur-sm max-w-md">
          <h4 className="font-bold text-sm text-white uppercase tracking-wider mb-3">Onboarding Roadmap</h4>
          <ol className="space-y-3 text-xs text-indigo-200">
            <li className="flex gap-2">
              <span className="font-bold text-white">01.</span> Verify your email via 6-digit verification code.
            </li>
            <li className="flex gap-2">
              <span className="font-bold text-white">02.</span> Setup profile details (Date of Birth, Username).
            </li>
            <li className="flex gap-2">
              <span className="font-bold text-white">03.</span> Select a platform role &amp; configure storefront (optional).
            </li>
          </ol>
        </div>

        <div className="text-xs text-indigo-300 font-medium">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* RIGHT SIDE: Wizard Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative">
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-slate-200 dark:border-slate-800 bg-white/70 dark:bg-slate-900/60 backdrop-blur-md">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-600 dark:from-indigo-400 dark:via-purple-400 dark:to-pink-400">
              Sign Up
            </h1>
            <p className="text-slate-500 dark:text-slate-400 text-sm mt-2 font-medium">
              Create your profile account
            </p>
          </div>

          {renderStepsIndicator()}

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

          {/* STEP 1: Verification and Password */}
          {regStep === 1 && (
            <div className="space-y-4">
              {!codeVerified ? (
                <>
                  <div>
                    <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Email Address</label>
                    <div className="flex gap-2">
                      <input
                        type="email"
                        required
                        disabled={codeSent}
                        placeholder="you@example.com"
                        className="flex-1 px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                      />
                      {!codeSent ? (
                        <button
                          type="button"
                          onClick={handleSendCode}
                          disabled={loading || !regEmail}
                          className="px-4 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-600/50 text-white rounded-lg text-xs font-semibold transition-all active:scale-95"
                        >
                          {loading ? 'Sending...' : 'Send'}
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => setCodeSent(false)}
                          className="px-4 border border-slate-300 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 rounded-lg text-xs font-semibold transition-all"
                        >
                          Change
                        </button>
                      )}
                    </div>
                  </div>

                  {codeSent && (
                    <div className="animate-fadeIn">
                      <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">6-Digit Verification Code</label>
                      <div className="flex gap-2">
                        <input
                          type="text"
                          maxLength={6}
                          required
                          placeholder="Enter code"
                          className="flex-1 px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white tracking-widest text-center font-mono"
                          value={code}
                          onChange={(e) => setCode(e.target.value)}
                        />
                        <button
                          type="button"
                          onClick={handleVerifyCode}
                          disabled={loading || code.length !== 6}
                          className="px-6 bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-600/50 text-white rounded-lg text-xs font-semibold transition-all active:scale-95"
                        >
                          {loading ? 'Verifying...' : 'Verify'}
                        </button>
                      </div>
                      <div className="text-right mt-2">
                        <button
                          type="button"
                          onClick={handleSendCode}
                          className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors"
                        >
                          Resend Code
                        </button>
                      </div>
                    </div>
                  )}
                </>
              ) : (
                <form onSubmit={handleInitiate} className="space-y-4 animate-fadeIn">
                  <div>
                    <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Set Password</label>
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                      value={regPassword}
                      onChange={(e) => setRegPassword(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Confirm Password</label>
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
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
                      'Create Account & Continue'
                    )}
                  </button>
                </form>
              )}
            </div>
          )}

          {/* STEP 2: Profile Details */}
          {regStep === 2 && (
            <form onSubmit={handleSaveProfile} className="space-y-4 animate-fadeIn">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">First Name</label>
                  <input
                    type="text"
                    required
                    placeholder="Jane"
                    className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Last Name</label>
                  <input
                    type="text"
                    required
                    placeholder="Doe"
                    className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Username</label>
                <input
                  type="text"
                  required
                  placeholder="janedoe"
                  className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Date of Birth</label>
                <input
                  type="date"
                  required
                  className="w-full px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:border-indigo-500 dark:text-white transition-colors"
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                />
              </div>

              <div className="flex items-start mt-2">
                <div className="flex items-center h-5">
                  <input
                    id="agreedTerms"
                    type="checkbox"
                    required
                    className="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-slate-300 bg-white dark:bg-slate-950 rounded"
                    checked={agreedTerms}
                    onChange={(e) => setAgreedTerms(e.target.checked)}
                  />
                </div>
                <div className="ml-3 text-sm">
                  <label htmlFor="agreedTerms" className="font-semibold text-slate-500 dark:text-slate-400 text-xs">
                    I agree to the Terms of Service and Privacy Policy
                  </label>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 dark:from-indigo-500 dark:to-purple-600 dark:hover:from-indigo-600 dark:hover:to-purple-700 text-white rounded-lg font-semibold text-sm transition-all shadow-lg active:scale-98 flex justify-center items-center"
              >
                {loading ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  'Save Profile Details'
                )}
              </button>
            </form>
          )}

          <div className="text-center mt-6">
            <Link
              to="/login"
              className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline transition-colors font-semibold"
            >
              Already have an account? Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
