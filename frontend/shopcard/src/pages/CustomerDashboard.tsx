import React, { useState, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../store';
import { logout } from '../store/authSlice';
import axiosClient from '../api/axiosClient';
import { useNavigate } from 'react-router-dom';
import SettingsPanel from '../components/SettingsPanel';

interface Shop {
  id: string;
  ownerId: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  phone: string;
  followerCount: number;
}

interface Product {
  id: string;
  name: string;
  description: string;
  sku: string;
  price: number;
  imageUrl: string;
}

interface CartItem {
  product: Product;
  quantity: number;
}

const CustomerDashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const { user } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();

  // Location settings Bangalore coordinates default
  const [latitude, setLatitude] = useState(12.9716);
  const [longitude, setLongitude] = useState(77.5946);
  const [radius, setRadius] = useState(10000); // 10km

  const [shops, setShops] = useState<Shop[]>([]);
  const [followedShops, setFollowedShops] = useState<string[]>([]);
  const [selectedShop, setSelectedShop] = useState<Shop | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [esSearchResults, setEsSearchResults] = useState<any[]>([]);

  // Cart
  const [cart, setCart] = useState<CartItem[]>([]);
  const [showCart, setShowCart] = useState(false);

  // Orders
  const [orders, setOrders] = useState<any[]>([]);
  const [recentPickupCode, setRecentPickupCode] = useState<string | null>(null);

  // Notifications
  const [notifications, setNotifications] = useState<any[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);

  // Settings Modal state
  const [showSettings, setShowSettings] = useState(false);

  useEffect(() => {
    fetchShops();
    fetchOrders();
    fetchNotifications();
    
    // Poll for notifications and orders updates
    const interval = setInterval(() => {
      fetchNotifications();
      fetchOrders();
    }, 10000);
    return () => clearInterval(interval);
  }, [latitude, longitude, radius]);

  const fetchShops = async () => {
    try {
      const res = await axiosClient.get(`/shops/nearby?longitude=${longitude}&latitude=${latitude}&radius=${radius}`);
      setShops(res.data);
    } catch (err) {
      console.error('Error fetching shops', err);
    }
  };

  const fetchOrders = async () => {
    try {
      const res = await axiosClient.get('/orders/customer');
      setOrders(res.data);
    } catch (err) {
      console.error('Error fetching orders', err);
    }
  };

  const fetchNotifications = async () => {
    try {
      const res = await axiosClient.get('/notifications');
      setNotifications(res.data);
    } catch (err) {
      console.error('Error fetching notifications', err);
    }
  };

  const handleFollowToggle = async (shopId: string) => {
    try {
      if (followedShops.includes(shopId)) {
        await axiosClient.post(`/shops/${shopId}/unfollow`);
        setFollowedShops(followedShops.filter(id => id !== shopId));
      } else {
        await axiosClient.post(`/shops/${shopId}/follow`);
        setFollowedShops([...followedShops, shopId]);
      }
      fetchShops();
    } catch (err) {
      console.error('Error following/unfollowing shop', err);
    }
  };

  const handleShopSelect = async (shop: Shop) => {
    setSelectedShop(shop);
    try {
      const res = await axiosClient.get(`/products?shopId=${shop.id}`);
      setProducts(res.data);
    } catch (err) {
      console.error('Error fetching products', err);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery) {
      setEsSearchResults([]);
      return;
    }
    try {
      const res = await axiosClient.get(`/products/search?query=${searchQuery}`);
      setEsSearchResults(res.data);
    } catch (err) {
      console.error('Elasticsearch search error', err);
    }
  };

  const addToCart = (product: Product) => {
    const existing = cart.find(item => item.product.id === product.id);
    if (existing) {
      setCart(cart.map(item => item.product.id === product.id ? { ...item, quantity: item.quantity + 1 } : item));
    } else {
      setCart([...cart, { product, quantity: 1 }]);
    }
  };

  const handleCheckout = async () => {
    if (!selectedShop || cart.length === 0) return;
    try {
      const payload = {
        shopId: selectedShop.id,
        items: cart.map(item => ({
          productId: item.product.id,
          name: item.product.name,
          price: item.product.price,
          quantity: item.quantity
        }))
      };
      const res = await axiosClient.post('/orders', payload);
      setRecentPickupCode(res.data.pickupCodePlain);
      setCart([]);
      setShowCart(false);
      fetchOrders();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Checkout failed');
    }
  };

  const markNotificationRead = async (id: number) => {
    try {
      await axiosClient.post(`/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="min-h-screen bg-brand-bg text-brand-text pb-12 transition-colors duration-300">
      {/* Header bar */}
      <header className="sticky top-0 bg-brand-card/85 backdrop-blur-md border-b border-brand-border px-6 py-4 flex justify-between items-center z-40 transition-colors">
        <div className="flex items-center gap-6">
          <h1 className="text-2xl font-black bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-600 dark:from-indigo-400 dark:to-violet-400">
            ShopCard Customer Portal
          </h1>
          <form onSubmit={handleSearch} className="relative hidden md:flex items-center">
            <input
              type="text"
              placeholder="Search products globally (Elasticsearch)..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="bg-brand-input border border-brand-input-border rounded-xl pl-10 pr-4 py-1.5 text-xs text-brand-text focus:outline-none focus:border-indigo-500 w-80 transition-colors"
            />
            {/* Search Icon */}
            <svg className="absolute left-3 w-4 h-4 text-brand-text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </form>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative p-2 bg-brand-card hover:bg-brand-bg/60 border border-brand-border rounded-xl transition-colors cursor-pointer"
          >
            <span className="text-sm">🔔</span>
            {notifications.filter(n => !n.isRead).length > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-[10px] text-white px-1.5 py-0.5 rounded-full font-bold">
                {notifications.filter(n => !n.isRead).length}
              </span>
            )}
          </button>

          <button
            onClick={() => setShowCart(!showCart)}
            className="p-2 bg-brand-card hover:bg-brand-bg/60 border border-brand-border rounded-xl transition-colors flex items-center gap-2 cursor-pointer"
          >
            <span className="text-sm">🛒</span>
            <span className="text-xs font-semibold text-brand-text">{cart.reduce((sum, i) => sum + i.quantity, 0)}</span>
          </button>

          <div className="text-right flex items-center gap-3">
            <div>
              <div className="text-sm font-semibold text-brand-text">{user?.firstName}</div>
              <div className="flex gap-2">
                <button 
                  onClick={() => setShowSettings(true)} 
                  className="text-[10px] text-indigo-500 hover:text-indigo-600 font-bold transition-colors cursor-pointer"
                >
                  Settings
                </button>
                <span className="text-[10px] text-brand-border">|</span>
                <button 
                  onClick={() => dispatch(logout())} 
                  className="text-[10px] text-brand-text-muted hover:text-red-400 font-semibold transition-colors cursor-pointer"
                >
                  Log out
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Body Grid */}
      <div className="max-w-7xl mx-auto px-6 mt-8 grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* Left Side: Geolocation filter & Shops list (4 cols) */}
        <div className="lg:col-span-4 space-y-6">
          <div className="bg-brand-card border border-brand-border rounded-2xl p-5 space-y-4">
            <h2 className="text-sm font-bold uppercase tracking-wider text-brand-text-muted">Discover Nearby Shops</h2>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-[10px] text-brand-text-muted uppercase mb-1">Latitude</label>
                <input
                  type="number"
                  step="0.0001"
                  value={latitude}
                  onChange={(e) => setLatitude(parseFloat(e.target.value))}
                  className="w-full bg-brand-input border border-brand-input-border rounded-xl px-2.5 py-1.5 text-xs text-brand-text"
                />
              </div>
              <div>
                <label className="block text-[10px] text-brand-text-muted uppercase mb-1">Longitude</label>
                <input
                  type="number"
                  step="0.0001"
                  value={longitude}
                  onChange={(e) => setLongitude(parseFloat(e.target.value))}
                  className="w-full bg-brand-input border border-brand-input-border rounded-xl px-2.5 py-1.5 text-xs text-brand-text"
                />
              </div>
            </div>

            <div>
              <label className="block text-[10px] text-brand-text-muted uppercase mb-1">Radius</label>
              <select
                value={radius}
                onChange={(e) => setRadius(parseInt(e.target.value))}
                className="w-full bg-brand-input border border-brand-input-border rounded-xl px-2.5 py-1.5 text-xs text-brand-text"
              >
                <option value="1000">1 km</option>
                <option value="5000">5 km</option>
                <option value="10000">10 km</option>
                <option value="50000">50 km</option>
              </select>
            </div>
          </div>

          {/* Shop List */}
          <div className="space-y-4">
            <h3 className="text-xs font-bold text-brand-text-muted uppercase">Shops in your area</h3>
            <div className="space-y-3 max-h-[400px] overflow-y-auto pr-1">
              {shops.length === 0 ? (
                <div className="p-4 bg-brand-card border border-brand-border rounded-xl text-center text-xs text-brand-text-muted">
                  No shops found nearby.
                </div>
              ) : (
                shops.map(shop => (
                  <div
                    key={shop.id}
                    onClick={() => handleShopSelect(shop)}
                    className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                      selectedShop?.id === shop.id
                        ? 'bg-indigo-500/10 border-indigo-500 shadow-md'
                        : 'bg-brand-card border-brand-border hover:border-brand-text-muted/40'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <h4 className="font-bold text-brand-text text-sm">{shop.name}</h4>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleFollowToggle(shop.id);
                        }}
                        className={`text-[10px] px-2 py-0.5 rounded font-bold transition-all cursor-pointer ${
                          followedShops.includes(shop.id)
                            ? 'bg-brand-bg text-indigo-500 border border-indigo-500/20'
                            : 'bg-indigo-600 text-white hover:bg-indigo-700'
                        }`}
                      >
                        {followedShops.includes(shop.id) ? 'Following' : 'Follow'}
                      </button>
                    </div>
                    <p className="text-brand-text-muted text-xs mt-1.5 line-clamp-2">{shop.description}</p>
                    <div className="flex justify-between text-[10px] text-brand-text-muted mt-3">
                      <span>📍 {shop.address}</span>
                      <span>👥 {shop.followerCount} followers</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Right Side: Products list or search outcomes (8 cols) */}
        <div className="lg:col-span-8 space-y-6">
          {esSearchResults.length > 0 && (
            <div className="bg-brand-card border border-brand-border rounded-2xl p-6">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-sm font-bold text-indigo-500 uppercase">Search Results (Elasticsearch)</h3>
                <button onClick={() => setEsSearchResults([])} className="text-xs text-brand-text-muted hover:text-brand-text">Clear</button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {esSearchResults.map(product => (
                  <div key={product.id} className="bg-brand-input p-4 border border-brand-input-border rounded-2xl flex justify-between items-center">
                    <div>
                      <h4 className="font-bold text-brand-text text-sm">{product.name}</h4>
                      <p className="text-xs text-brand-text-muted line-clamp-1">{product.description}</p>
                      <div className="text-xs font-bold text-indigo-500 mt-2">${product.price}</div>
                    </div>
                    <button
                      onClick={() => addToCart(product)}
                      className="px-3 py-1.5 bg-indigo-600 text-white rounded-lg text-xs hover:bg-indigo-700 transition-colors font-bold cursor-pointer"
                    >
                      + Add
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Catalog products */}
          {selectedShop ? (
            <div className="bg-brand-card border border-brand-border rounded-2xl p-6">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h3 className="text-lg font-black text-brand-text">{selectedShop.name}</h3>
                  <p className="text-xs text-brand-text-muted mt-1">{selectedShop.address} • {selectedShop.phone}</p>
                </div>
                <button
                  onClick={() => navigate(`/chat?recipientId=${selectedShop.ownerId}&name=${selectedShop.name}`)}
                  className="px-3.5 py-1.5 bg-violet-600 text-white rounded-xl text-xs hover:bg-violet-700 font-bold transition-all flex items-center gap-1.5 cursor-pointer active:scale-95"
                >
                  💬 Chat with Owner
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {products.length === 0 ? (
                  <div className="col-span-3 text-center py-12 text-brand-text-muted text-xs">
                    No products listed in this storefront's catalog yet.
                  </div>
                ) : (
                  products.map(product => (
                    <div key={product.id} className="bg-brand-input rounded-2xl border border-brand-input-border overflow-hidden flex flex-col justify-between p-4">
                      <div>
                        {product.imageUrl ? (
                          <img src={product.imageUrl} alt={product.name} className="w-full h-32 object-cover rounded-xl mb-3" />
                        ) : (
                          <div className="w-full h-32 bg-brand-bg rounded-xl flex items-center justify-center mb-3">
                            <span className="text-2xl">📦</span>
                          </div>
                        )}
                        <h4 className="font-bold text-brand-text text-sm">{product.name}</h4>
                        <p className="text-brand-text-muted text-xs mt-1 line-clamp-2">{product.description}</p>
                      </div>
                      <div className="flex justify-between items-center mt-4">
                        <span className="text-sm font-extrabold text-indigo-500">${product.price}</span>
                        <button
                          onClick={() => addToCart(product)}
                          className="px-2.5 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-lg transition-colors cursor-pointer"
                        >
                          + Add
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          ) : (
            <div className="h-64 bg-brand-card border border-brand-border rounded-2xl flex items-center justify-center text-brand-text-muted text-xs font-semibold">
              Select a shop from the sidebar to view products
            </div>
          )}

          {/* Active Pickups & Orders Log */}
          <div className="bg-brand-card border border-brand-border rounded-2xl p-6">
            <h3 className="text-xs font-bold text-brand-text-muted uppercase mb-4">Click &amp; Collect Orders</h3>
            
            {recentPickupCode && (
              <div className="bg-indigo-500/10 border border-indigo-500/20 rounded-2xl p-5 mb-6 text-center">
                <span className="text-xs text-indigo-500 block font-bold">Your Click &amp; Collect Pickup OTP Code</span>
                <span className="text-3xl font-black text-brand-text tracking-widest block my-2">{recentPickupCode}</span>
                <span className="text-[10px] text-brand-text-muted">Provide this code to the owner at the shop counter. Valid for 48 hours.</span>
              </div>
            )}

            <div className="space-y-4 max-h-80 overflow-y-auto pr-1">
              {orders.length === 0 ? (
                <div className="text-center py-6 text-brand-text-muted text-xs font-semibold">
                  You haven't placed any orders yet.
                </div>
              ) : (
                orders.map(order => (
                  <div key={order.id} className="bg-brand-input border border-brand-input-border p-4 rounded-xl flex justify-between items-center text-xs">
                    <div>
                      <span className="text-brand-text-muted block">Order ID: #{order.id.substring(0, 8)}</span>
                      <span className="text-brand-text block font-bold mt-1">Total: ${order.totalAmount}</span>
                    </div>
                    <div className="text-right">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                        order.status === 'READY_FOR_PICKUP' ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' :
                        order.status === 'PREPARING' ? 'bg-amber-500/10 text-amber-600 border-amber-500/20' :
                        order.status === 'PICKED_UP' ? 'bg-brand-bg text-brand-text-muted border-brand-border' : 'bg-red-500/10 text-red-600 border-red-500/20'
                      }`}>
                        {order.status}
                      </span>
                      <span className="block text-[10px] text-brand-text-muted mt-2">Placed: {new Date(order.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Cart Drawer */}
      {showCart && (
        <div className="fixed inset-0 bg-black/60 z-50 flex justify-end">
          <div className="w-full max-w-md bg-brand-card border-l border-brand-border h-full p-6 flex flex-col justify-between">
            <div>
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-lg font-black text-brand-text">Shopping Cart</h3>
                <button onClick={() => setShowCart(false)} className="text-brand-text-muted hover:text-brand-text text-sm cursor-pointer font-semibold">Close</button>
              </div>

              <div className="space-y-4 overflow-y-auto max-h-[60vh] pr-1">
                {cart.length === 0 ? (
                  <div className="text-center py-12 text-brand-text-muted text-xs font-semibold">
                    Your cart is empty.
                  </div>
                ) : (
                  cart.map(item => (
                    <div key={item.product.id} className="flex justify-between items-center bg-brand-input p-3 rounded-xl border border-brand-input-border">
                      <div>
                        <h4 className="font-bold text-brand-text text-xs">{item.product.name}</h4>
                        <span className="text-[10px] text-brand-text-muted">Qty: {item.quantity}</span>
                      </div>
                      <span className="text-xs font-bold text-indigo-500">${item.product.price * item.quantity}</span>
                    </div>
                  ))
                )}
              </div>
            </div>

            {cart.length > 0 && (
              <div className="pt-6 border-t border-brand-border space-y-4">
                <div className="flex justify-between text-sm font-bold text-brand-text">
                  <span>Subtotal</span>
                  <span>${cart.reduce((sum, i) => sum + (i.product.price * i.quantity), 0)}</span>
                </div>
                <button
                  onClick={handleCheckout}
                  className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition-all shadow-md active:scale-98 cursor-pointer"
                >
                  Place Click &amp; Collect Order
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Notifications Drawer */}
      {showNotifications && (
        <div className="fixed inset-0 bg-black/60 z-50 flex justify-end">
          <div className="w-full max-w-md bg-brand-card border-l border-brand-border h-full p-6 flex flex-col">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-lg font-black text-brand-text">Alerts &amp; Notifications</h3>
              <button onClick={() => setShowNotifications(false)} className="text-brand-text-muted hover:text-brand-text text-sm cursor-pointer font-semibold">Close</button>
            </div>
            
            <div className="space-y-4 overflow-y-auto flex-1 pr-1">
              {notifications.length === 0 ? (
                <div className="text-center py-12 text-brand-text-muted text-xs font-semibold">
                  No notifications yet.
                </div>
              ) : (
                notifications.map(notif => (
                  <div key={notif.id} className={`p-4 rounded-xl border flex flex-col justify-between ${
                    notif.isRead ? 'bg-brand-bg/50 border-brand-border text-brand-text-muted' : 'bg-indigo-500/5 border-indigo-500/20 text-brand-text'
                  }`}>
                    <p className="text-xs font-medium">{notif.message}</p>
                    <div className="flex justify-between items-center mt-3 text-[10px] text-brand-text-muted">
                      <span>{new Date(notif.timestamp).toLocaleTimeString()}</span>
                      {!notif.isRead && (
                        <button onClick={() => markNotificationRead(notif.id)} className="text-indigo-500 hover:underline font-bold">Mark read</button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* Settings Modal */}
      {showSettings && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-xl bg-brand-bg border border-brand-border rounded-3xl p-6 shadow-2xl overflow-y-auto max-h-[90vh] animate-fadeIn">
            <SettingsPanel onClose={() => setShowSettings(false)} />
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerDashboard;
