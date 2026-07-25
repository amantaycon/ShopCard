import React, { useState } from 'react';
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
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

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
              <div className={`w-16 h-0.5 mx-2 ${regStep >= step.num ? 'bg-emerald-500' : 'bg-slate-200 dark:bg-slate-800'}`} />
            )}
            <div className="flex flex-col items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-300 ${
                regStep === step.num
                  ? 'bg-emerald-600 dark:bg-emerald-500 text-white ring-4 ring-emerald-500/20 scale-110'
                  : regStep > step.num
                  ? 'bg-emerald-500 text-white'
                  : 'bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-800 text-slate-400'
              }`}>
                {regStep > step.num ? '✓' : step.num}
              </div>
              <span className={`text-[10px] mt-1 font-semibold uppercase tracking-wider ${
                regStep === step.num ? 'text-emerald-600 dark:text-emerald-400' : regStep > step.num ? 'text-emerald-500' : 'text-slate-400'
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
            Secure Registration
          </span>
          <h2 className="text-5xl font-black mt-6 tracking-tight leading-tight bg-gradient-to-br from-emerald-950 via-emerald-900 to-teal-950 dark:from-white dark:to-emerald-300 bg-clip-text text-transparent">
            Join the local<br />neighborhood net.
          </h2>
          <p className="text-emerald-800/80 dark:text-emerald-200/80 mt-4 text-lg font-medium max-w-md leading-relaxed">
            Verify your email and complete your personal profile to start using the ShopCard click &amp; collect app.
          </p>
        </div>

        {/* Informative Step Box */}
        <div className="bg-white/40 dark:bg-emerald-950/20 border border-emerald-200/30 dark:border-emerald-800/20 rounded-2xl p-6 backdrop-blur-md max-w-md relative z-10">
          <h4 className="font-bold text-sm text-emerald-900 dark:text-emerald-100 uppercase tracking-wider mb-3">Onboarding Roadmap</h4>
          <ol className="space-y-3 text-xs text-emerald-800/90 dark:text-emerald-300/85">
            <li className="flex gap-2">
              <span className="font-bold text-emerald-700 dark:text-emerald-400">01.</span> Verify your email via 6-digit verification code.
            </li>
            <li className="flex gap-2">
              <span className="font-bold text-emerald-700 dark:text-emerald-400">02.</span> Setup profile details (Date of Birth, Username).
            </li>
            <li className="flex gap-2">
              <span className="font-bold text-emerald-700 dark:text-emerald-400">03.</span> Select a platform role &amp; configure storefront (optional).
            </li>
          </ol>
        </div>

        <div className="text-xs text-emerald-800/40 dark:text-emerald-400/40 font-semibold relative z-10">
          © 2026 ShopCard Platform. All Rights Reserved.
        </div>
      </div>

      {/* Background/Vertical Divider with Gradient */}
      <div className="hidden lg:block w-[1.5px] bg-gradient-to-b from-emerald-200 via-teal-400 to-emerald-500 dark:from-emerald-950 dark:via-teal-850 dark:to-emerald-950 opacity-60 self-stretch z-10 relative"></div>

      {/* RIGHT SIDE: Wizard Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center px-6 py-12 relative z-10">
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-emerald-500/5 dark:bg-emerald-500/10 rounded-full blur-3xl -z-10 animate-pulse"></div>

        <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-emerald-100/50 dark:border-emerald-800/20 bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl shadow-[0_20px_50px_rgba(16,185,129,0.06)] dark:shadow-none">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-emerald-600 via-teal-500 to-emerald-700 dark:from-emerald-400 dark:via-teal-300 dark:to-emerald-500">
              Sign Up
            </h1>
            <p className="text-emerald-800/70 dark:text-emerald-400/60 text-sm mt-2 font-medium">
              Create your profile account
            </p>
          </div>

          {renderStepsIndicator()}

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
                        className="flex-1 px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                      />
                      {!codeSent ? (
                        <button
                          type="button"
                          onClick={handleSendCode}
                          disabled={loading || !regEmail}
                          className="px-4 bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-600/50 text-white rounded-xl text-xs font-bold transition-all active:scale-95 cursor-pointer"
                        >
                          {loading ? 'Sending...' : 'Send'}
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => setCodeSent(false)}
                          className="px-4 border border-slate-200 dark:border-slate-800 hover:border-emerald-300 dark:hover:border-emerald-800 hover:bg-emerald-50/20 dark:hover:bg-emerald-950/20 text-slate-500 dark:text-slate-400 rounded-xl text-xs font-semibold transition-all cursor-pointer"
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
                          className="flex-1 px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white tracking-widest text-center font-mono transition-all duration-200 shadow-sm"
                          value={code}
                          onChange={(e) => setCode(e.target.value)}
                        />
                        <button
                          type="button"
                          onClick={handleVerifyCode}
                          disabled={loading || code.length !== 6}
                          className="px-6 bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-600/50 text-white rounded-xl text-xs font-bold transition-all active:scale-95 cursor-pointer"
                        >
                          {loading ? 'Verifying...' : 'Verify'}
                        </button>
                      </div>
                      <div className="text-right mt-2">
                        <button
                          type="button"
                          onClick={handleSendCode}
                          className="text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors font-semibold cursor-pointer"
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
                    <div className="relative">
                      <input
                        type={showPassword ? 'text' : 'password'}
                        required
                        placeholder="••••••••"
                        className="w-full pl-4 pr-10 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                        value={regPassword}
                        onChange={(e) => setRegPassword(e.target.value)}
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
                  <div>
                    <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Confirm Password</label>
                    <div className="relative">
                      <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        required
                        placeholder="••••••••"
                        className="w-full pl-4 pr-10 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-emerald-500 transition-colors cursor-pointer"
                      >
                        {showConfirmPassword ? (
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
                    className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
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
                    className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
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
                  className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Date of Birth</label>
                <input
                  type="date"
                  required
                  className="w-full px-4 py-2.5 bg-white/70 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800 rounded-xl text-sm focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 dark:text-white transition-all duration-200 shadow-sm"
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
                    className="focus:ring-emerald-500 h-4 w-4 text-emerald-600 border-slate-350 bg-white dark:bg-slate-950 rounded cursor-pointer"
                    checked={agreedTerms}
                    onChange={(e) => setAgreedTerms(e.target.checked)}
                  />
                </div>
                <div className="ml-3 text-sm">
                  <label htmlFor="agreedTerms" className="font-semibold text-slate-550 dark:text-slate-400 text-xs">
                    I agree to the Terms of Service and Privacy Policy
                  </label>
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
                  'Save Profile Details'
                )}
              </button>
            </form>
          )}

          <div className="text-center mt-6">
            <Link
              to="/login"
              className="text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors font-semibold"
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
