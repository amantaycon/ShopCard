import React, { useState } from 'react';
import { useAppDispatch, useAppSelector } from '../store';
import { updateUser, setShopId } from '../store/authSlice';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const Onboarding: React.FC = () => {
  const dispatch = useAppDispatch();
  const { user } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();

  const [step, setStep] = useState(1); // Step 1: Role Selection Modal, Step 2: Storefront Details Form
  const [selectedRole, setSelectedRole] = useState<'CUSTOMER' | 'SHOP_OWNER' | 'DELIVERY_PARTNER'>('CUSTOMER');

  // Status flags
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Business Details fields
  const [shopName, setShopName] = useState('');
  const [shopDescription, setShopDescription] = useState('');
  const [shopAddress, setShopAddress] = useState('');
  const [shopLatitude, setShopLatitude] = useState(12.9716); // Bangalore coordinate default
  const [shopLongitude, setShopLongitude] = useState(77.5946);
  const [shopPhone, setShopPhone] = useState('');
  const [shopEmail, setShopEmail] = useState('');
  const [shopLogoUrl, setShopLogoUrl] = useState('');
  const [shopBannerUrl, setShopBannerUrl] = useState('');
  const [businessType] = useState('Shop');
  const [shopType, setShopType] = useState('General Store');

  const handleSaveRole = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await axiosClient.put('/auth/role', { role: selectedRole });
      dispatch(updateUser({ roles: [`ROLE_${selectedRole}`] }));
      
      if (selectedRole === 'SHOP_OWNER') {
        setSuccess('Role selected successfully! Initializing business registration details...');
        setTimeout(() => {
          setStep(2);
          setSuccess('');
        }, 1200);
      } else {
        setSuccess('Onboarding complete! Preparing your personalized dashboard...');
        setTimeout(() => {
          navigate('/');
        }, 1500);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to assign profile role.');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveShop = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await axiosClient.post('/shops', {
        name: shopName,
        description: shopDescription,
        address: shopAddress,
        latitude: shopLatitude,
        longitude: shopLongitude,
        phone: shopPhone,
        email: shopEmail || user?.email || '',
        logoUrl: shopLogoUrl,
        bannerUrl: shopBannerUrl,
        businessType,
        shopType
      });
      
      if (res.data && res.data.id) {
        dispatch(setShopId(res.data.id));
      }
      setSuccess('Business registered successfully! Redirecting to merchant control center...');
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to register shop storefront.');
    } finally {
      setLoading(false);
    }
  };

  const handleGetLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setShopLatitude(position.coords.latitude);
          setShopLongitude(position.coords.longitude);
          setSuccess('Device GPS coordinates resolved successfully.');
          setTimeout(() => setSuccess(''), 2500);
        },
        () => {
          setError('Could not access device location. Please input coordinates manually.');
        }
      );
    } else {
      setError('Geolocation is not supported by your browser.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-slate-950 dark:via-emerald-950/20 dark:to-slate-950 text-slate-800 dark:text-slate-100 flex flex-col justify-center items-center py-12 px-4 relative overflow-hidden transition-colors duration-300">
      
      {/* Animated glowing mesh gradients matching brand identity */}
      <div className="absolute top-0 left-0 w-[500px] h-[500px] rounded-full bg-emerald-500/10 blur-3xl -translate-x-1/2 -translate-y-1/2 animate-pulse"></div>
      <div className="absolute bottom-0 right-0 w-[500px] h-[500px] rounded-full bg-teal-500/10 blur-3xl translate-x-1/2 translate-y-1/2 animate-pulse"></div>
      <div className="absolute top-1/2 left-1/2 w-[600px] h-[600px] rounded-full bg-emerald-400/5 blur-3xl -translate-x-1/2 -translate-y-1/2"></div>

      {/* STEP 1: INTERACTIVE POPUP / MODAL OVERLAY */}
      {step === 1 && (
        <div className="w-full max-w-2xl z-15 transform transition-all duration-500 animate-fadeIn">
          <div className="glass-panel rounded-3xl p-8 border border-emerald-100/50 dark:border-emerald-800/30 text-center shadow-2xl relative bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl">
            
            {/* Header branding */}
            <div className="mb-8">
              <span className="px-3.5 py-1 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 rounded-full text-[10px] font-extrabold uppercase tracking-widest border border-emerald-500/20">
                Setup Account
              </span>
              <h2 className="text-3xl font-black tracking-tight text-brand-text mt-4">
                Select Your ShopCard Role
              </h2>
              <p className="text-brand-text-muted text-xs mt-2 max-w-md mx-auto">
                Hi {user?.firstName || 'there'}! Tell us how you plan to use the ShopCard network. You can change themes or profile details later in settings.
              </p>
            </div>

            {error && (
              <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-xl mb-6 text-center font-semibold">
                {error}
              </div>
            )}
            {success && (
              <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs px-4 py-3 rounded-xl mb-6 text-center font-semibold">
                {success}
              </div>
            )}

            {/* Role Options */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5 text-left mb-8">
              {/* Customer Option */}
              <div
                onClick={() => setSelectedRole('CUSTOMER')}
                className={`p-5 rounded-2xl border-2 cursor-pointer transition-all duration-300 relative group flex flex-col justify-between ${
                  selectedRole === 'CUSTOMER'
                    ? 'border-emerald-500 bg-emerald-500/5 ring-4 ring-emerald-500/10 shadow-lg scale-102'
                    : 'border-brand-border bg-brand-card/45 hover:border-brand-border/80'
                }`}
              >
                <div>
                  <div className="text-3xl mb-3 transition-transform group-hover:scale-110 duration-300">👤</div>
                  <h4 className="text-sm font-bold text-brand-text">Customer</h4>
                  <p className="text-[10px] text-brand-text-muted mt-2 leading-relaxed font-medium">
                    Order items, follow local catalog updates, and use secure OTP codes for collection.
                  </p>
                </div>
                {selectedRole === 'CUSTOMER' && (
                  <span className="absolute top-3 right-3 text-xs bg-emerald-500 text-white rounded-full w-5 h-5 flex items-center justify-center font-bold animate-pulse">✓</span>
                )}
              </div>

              {/* Shop Owner Option */}
              <div
                onClick={() => setSelectedRole('SHOP_OWNER')}
                className={`p-5 rounded-2xl border-2 cursor-pointer transition-all duration-300 relative group flex flex-col justify-between ${
                  selectedRole === 'SHOP_OWNER'
                    ? 'border-emerald-500 bg-emerald-500/5 ring-4 ring-emerald-500/10 shadow-lg scale-102'
                    : 'border-brand-border bg-brand-card/45 hover:border-brand-border/80'
                }`}
              >
                <div>
                  <div className="text-3xl mb-3 transition-transform group-hover:scale-110 duration-300">🏪</div>
                  <h4 className="text-sm font-bold text-brand-text">Shop Owner</h4>
                  <p className="text-[10px] text-brand-text-muted mt-2 leading-relaxed font-medium">
                    Establish storefront, publish product stock, fulfill order collections, and manage catalog.
                  </p>
                </div>
                {selectedRole === 'SHOP_OWNER' && (
                  <span className="absolute top-3 right-3 text-xs bg-emerald-500 text-white rounded-full w-5 h-5 flex items-center justify-center font-bold animate-pulse">✓</span>
                )}
              </div>

              {/* Delivery Partner Option */}
              <div
                onClick={() => setSelectedRole('DELIVERY_PARTNER')}
                className={`p-5 rounded-2xl border-2 cursor-pointer transition-all duration-300 relative group flex flex-col justify-between ${
                  selectedRole === 'DELIVERY_PARTNER'
                    ? 'border-emerald-500 bg-emerald-500/5 ring-4 ring-emerald-500/10 shadow-lg scale-102'
                    : 'border-brand-border bg-brand-card/45 hover:border-brand-border/80'
                }`}
              >
                <div>
                  <div className="text-3xl mb-3 transition-transform group-hover:scale-110 duration-300">🚴</div>
                  <h4 className="text-sm font-bold text-brand-text">Delivery</h4>
                  <p className="text-[10px] text-brand-text-muted mt-2 leading-relaxed font-medium">
                    Support local merchants, deliver collection orders to clients, and track tasks.
                  </p>
                </div>
                {selectedRole === 'DELIVERY_PARTNER' && (
                  <span className="absolute top-3 right-3 text-xs bg-emerald-500 text-white rounded-full w-5 h-5 flex items-center justify-center font-bold animate-pulse">✓</span>
                )}
              </div>
            </div>

            {/* Action buttons */}
            <button
              onClick={handleSaveRole}
              disabled={loading}
              className="w-full py-3.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-xs transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 active:scale-98 flex justify-center items-center cursor-pointer"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : selectedRole === 'SHOP_OWNER' ? (
                'Setup Storefront details →'
              ) : (
                'Complete Account Setup'
              )}
            </button>
          </div>
        </div>
      )}

      {/* STEP 2: BUSINESS DETAILS REGISTER (ONLY VISIBLE FOR SHOP OWNER ROLE) */}
      {step === 2 && (
        <div className="w-full max-w-2xl z-15 transform transition-all duration-500 animate-slideUp">
          <div className="glass-panel rounded-3xl p-8 border border-emerald-100/50 dark:border-emerald-800/30 shadow-2xl bg-white/70 dark:bg-emerald-950/40 backdrop-blur-xl">
            
            <div className="text-center mb-6">
              <span className="px-3.5 py-1 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 rounded-full text-[10px] font-extrabold uppercase tracking-widest border border-emerald-500/20">
                Step 2: Business details
              </span>
              <h2 className="text-2xl font-black tracking-tight text-brand-text mt-3">
                Register Storefront Profile
              </h2>
              <p className="text-brand-text-muted text-xs mt-1">
                Provide details to help customers find your storefront on the catalog map
              </p>
            </div>

            {error && (
              <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs px-4 py-3 rounded-xl mb-4 text-center font-semibold animate-pulse">
                {error}
              </div>
            )}
            {success && (
              <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs px-4 py-3 rounded-xl mb-4 text-center font-semibold">
                {success}
              </div>
            )}

            <form onSubmit={handleSaveShop} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Business Category</label>
                  <input
                    type="text"
                    readOnly
                    className="w-full px-3.5 py-2.5 bg-brand-bg/40 border border-brand-border rounded-xl text-xs text-brand-text-muted cursor-not-allowed outline-none"
                    value={businessType}
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Service Specialty</label>
                  <select
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopType}
                    onChange={(e) => setShopType(e.target.value)}
                  >
                    <option value="General Store">General Store</option>
                    <option value="Stationeries">Stationeries</option>
                    <option value="Saloon">Saloon</option>
                    <option value="Supermarket">Supermarket</option>
                    <option value="Pharmacy">Pharmacy</option>
                    <option value="Restaurant">Restaurant</option>
                    <option value="Other Local Services">Other Local Services</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Store / Shop Name</label>
                <input
                  type="text"
                  required
                  placeholder="Central Market Grocery"
                  className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                  value={shopName}
                  onChange={(e) => setShopName(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Store Description</label>
                <input
                  type="text"
                  placeholder="Organic farm produce, dairy, bakery, and daily essentials"
                  className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                  value={shopDescription}
                  onChange={(e) => setShopDescription(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Physical Address</label>
                <input
                  type="text"
                  required
                  placeholder="Broadway Ave 42nd St, NYC"
                  className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                  value={shopAddress}
                  onChange={(e) => setShopAddress(e.target.value)}
                />
              </div>

              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider">Map Coordinates</label>
                  <button
                    type="button"
                    onClick={handleGetLocation}
                    className="text-[10px] text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 hover:underline transition-colors font-bold cursor-pointer"
                  >
                    📍 Get Current Location
                  </button>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <input
                    type="number"
                    step="any"
                    required
                    placeholder="Latitude"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopLatitude}
                    onChange={(e) => setShopLatitude(parseFloat(e.target.value))}
                  />
                  <input
                    type="number"
                    step="any"
                    required
                    placeholder="Longitude"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopLongitude}
                    onChange={(e) => setShopLongitude(parseFloat(e.target.value))}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Contact Phone</label>
                  <input
                    type="text"
                    placeholder="+1-555-0100"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopPhone}
                    onChange={(e) => setShopPhone(e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Contact Email</label>
                  <input
                    type="email"
                    placeholder="shop@example.com"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopEmail}
                    onChange={(e) => setShopEmail(e.target.value)}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Logo Image URL</label>
                  <input
                    type="text"
                    placeholder="https://example.com/logo.png"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopLogoUrl}
                    onChange={(e) => setShopLogoUrl(e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-brand-text-muted uppercase tracking-wider mb-1">Banner Image URL</label>
                  <input
                    type="text"
                    placeholder="https://example.com/banner.png"
                    className="w-full px-3.5 py-2.5 bg-brand-input border border-brand-input-border rounded-xl text-xs text-brand-text focus:outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all duration-200"
                    value={shopBannerUrl}
                    onChange={(e) => setShopBannerUrl(e.target.value)}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold text-xs transition-all shadow-md hover:shadow-lg shadow-emerald-600/10 active:scale-98 flex justify-center items-center mt-6 cursor-pointer"
              >
                {loading ? (
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  'Launch Storefront Dashboard ✓'
                )}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Onboarding;
