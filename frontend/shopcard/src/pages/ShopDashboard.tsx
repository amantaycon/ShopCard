import React, { useState, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../store';
import { setShopId, logout } from '../store/authSlice';
import axiosClient from '../api/axiosClient';
import { useNavigate } from 'react-router-dom';
import SettingsPanel from '../components/SettingsPanel';

const ShopDashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const { user, shopId } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();
  const [showSettings, setShowSettings] = useState(false);

  // Profile forms
  const [shopName, setShopName] = useState('');
  const [shopDesc, setShopDesc] = useState('');
  const [shopAddress, setShopAddress] = useState('');
  const [shopLat, setShopLat] = useState(12.9716);
  const [shopLon, setShopLon] = useState(77.5946);
  const [shopPhone, setShopPhone] = useState('');
  const [shopEmail, setShopEmail] = useState('');

  // Products
  const [products, setProducts] = useState<any[]>([]);
  const [newProductName, setNewProductName] = useState('');
  const [newProductDesc, setNewProductDesc] = useState('');
  const [newProductSku, setNewProductSku] = useState('');
  const [newProductPrice, setNewProductPrice] = useState('');
  const [newProductCategory, setNewProductCategory] = useState('');
  const [showProductModal, setShowProductModal] = useState(false);

  // Bulk Import
  const [importFile, setImportFile] = useState<File | null>(null);
  const [activeJob, setActiveJob] = useState<any>(null);

  // Orders
  const [orders, setOrders] = useState<any[]>([]);
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otpTargetOrderId, setOtpTargetOrderId] = useState<string | null>(null);
  const [otpCode, setOtpCode] = useState('');

  // Inventory
  const [inventory, setInventory] = useState<any[]>([]);
  const [showStockInModal, setShowStockInModal] = useState(false);
  const [stockInProductId, setStockInProductId] = useState<string | null>(null);
  const [stockInQty, setStockInQty] = useState('');

  // Tabs
  const [activeTab, setActiveTab] = useState<'profile' | 'products' | 'inventory' | 'orders' | 'import'>('profile');

  useEffect(() => {
    if (shopId) {
      fetchShopDetails();
      fetchProducts();
      fetchOrders();
      fetchInventory();
    }
  }, [shopId]);

  useEffect(() => {
    if (shopId && activeTab === 'inventory') {
      fetchInventory();
    }
  }, [activeTab]);

  useEffect(() => {
    let interval: any;
    if (activeJob && (activeJob.status === 'PENDING' || activeJob.status === 'PROCESSING')) {
      interval = setInterval(async () => {
        try {
          const res = await axiosClient.get(`/products/import/jobs/${activeJob.id}`);
          setActiveJob(res.data);
        } catch (err) {
          console.error(err);
        }
      }, 3000);
    }
    return () => clearInterval(interval);
  }, [activeJob]);

  const fetchShopDetails = async () => {
    try {
      const res = await axiosClient.get(`/shops/${shopId}`);
      setShopName(res.data.name);
      setShopDesc(res.data.description);
      setShopAddress(res.data.address);
      setShopLat(res.data.latitude);
      setShopLon(res.data.longitude);
      setShopPhone(res.data.phone);
      setShopEmail(res.data.email);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await axiosClient.get(`/products?shopId=${shopId}`);
      setProducts(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchOrders = async () => {
    try {
      const res = await axiosClient.get(`/orders/shop?shopId=${shopId}`);
      setOrders(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchInventory = async () => {
    try {
      const res = await axiosClient.get(`/inventory?shopId=${shopId}`);
      setInventory(res.data);
    } catch (err) {
      console.error('Error fetching inventory', err);
    }
  };

  const handleStockIn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!stockInProductId || !stockInQty) return;
    try {
      const payload = {
        productId: stockInProductId,
        quantity: parseInt(stockInQty)
      };
      await axiosClient.post(`/inventory/stock-in?shopId=${shopId}`, payload);
      setShowStockInModal(false);
      setStockInProductId(null);
      setStockInQty('');
      fetchInventory();
      alert('Stock updated successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update stock');
    }
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        name: shopName,
        description: shopDesc,
        address: shopAddress,
        latitude: shopLat,
        longitude: shopLon,
        phone: shopPhone,
        email: shopEmail
      };
      const res = await axiosClient.post('/shops', payload);
      dispatch(setShopId(res.data.id));
      alert('Shop profile saved successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save shop profile');
    }
  };

  const handleCreateProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        name: newProductName,
        description: newProductDesc,
        sku: newProductSku,
        price: parseFloat(newProductPrice),
        categoryName: newProductCategory
      };
      await axiosClient.post(`/products?shopId=${shopId}`, payload);
      setShowProductModal(false);
      
      // Clear inputs
      setNewProductName('');
      setNewProductDesc('');
      setNewProductSku('');
      setNewProductPrice('');
      setNewProductCategory('');

      fetchProducts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Product creation failed');
    }
  };

  const handleBulkImportSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!importFile) return;
    try {
      const formData = new FormData();
      formData.append('file', importFile);
      const res = await axiosClient.post(`/products/import?shopId=${shopId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setActiveJob(res.data);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Bulk import upload failed');
    }
  };

  const handleMarkReady = async (orderId: string) => {
    try {
      await axiosClient.post(`/orders/${orderId}/ready?shopId=${shopId}`);
      fetchOrders();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Update failed');
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await axiosClient.post(`/orders/${otpTargetOrderId}/pickup?shopId=${shopId}`, { pickupCode: otpCode });
      setShowOtpModal(false);
      setOtpCode('');
      setOtpTargetOrderId(null);
      fetchOrders();
      alert('OTP code verified! Product pickup marked complete.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'OTP Verification failed');
    }
  };

  return (
    <div className="min-h-screen bg-brand-bg text-brand-text flex flex-col transition-colors duration-300">
      {/* Header */}
      <header className="bg-brand-card/85 backdrop-blur-md border-b border-brand-border px-6 py-4 flex justify-between items-center z-40 transition-colors">
        <div>
          <h1 className="text-xl font-black bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-600 dark:from-indigo-400 dark:to-violet-400">
            ShopCard Owner Console
          </h1>
          <p className="text-[10px] text-brand-text-muted">Configure catalog and manage local pickups</p>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-sm font-semibold text-brand-text-muted">Owner ID: {user?.userId.substring(0, 8)}</span>
          <button 
            onClick={() => setShowSettings(true)} 
            className="text-xs text-indigo-500 hover:text-indigo-600 font-bold cursor-pointer"
          >
            Settings
          </button>
          <span className="text-xs text-brand-border">|</span>
          <button 
            onClick={() => dispatch(logout())} 
            className="text-xs text-brand-text-muted hover:text-red-455 font-bold cursor-pointer"
          >
            Logout
          </button>
        </div>
      </header>

      {/* Grid container */}
      <div className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 grid grid-cols-1 md:grid-cols-12 gap-8">
        
        {/* Navigation Sidebar (3 cols) */}
        <div className="md:col-span-3 space-y-2">
          <button
            onClick={() => setActiveTab('profile')}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm font-semibold transition-all border cursor-pointer ${
              activeTab === 'profile'
                ? 'bg-indigo-600 text-white border-indigo-500 shadow-md'
                : 'bg-brand-card hover:bg-brand-bg/60 border-brand-border text-brand-text-muted hover:text-brand-text'
            }`}
          >
            🏪 Shop Profile
          </button>
          <button
            disabled={!shopId}
            onClick={() => setActiveTab('products')}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm font-semibold transition-all border cursor-pointer disabled:opacity-40 ${
              activeTab === 'products'
                ? 'bg-indigo-600 text-white border-indigo-500 shadow-md'
                : 'bg-brand-card hover:bg-brand-bg/60 border-brand-border text-brand-text-muted hover:text-brand-text'
            }`}
          >
            📦 Products Catalog
          </button>
          <button
            disabled={!shopId}
            onClick={() => setActiveTab('inventory')}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm font-semibold transition-all border cursor-pointer disabled:opacity-40 ${
              activeTab === 'inventory'
                ? 'bg-indigo-600 text-white border-indigo-500 shadow-md'
                : 'bg-brand-card hover:bg-brand-bg/60 border-brand-border text-brand-text-muted hover:text-brand-text'
            }`}
          >
            📊 Stock Inventory
          </button>
          <button
            disabled={!shopId}
            onClick={() => setActiveTab('orders')}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm font-semibold transition-all border cursor-pointer disabled:opacity-40 ${
              activeTab === 'orders'
                ? 'bg-indigo-600 text-white border-indigo-500 shadow-md'
                : 'bg-brand-card hover:bg-brand-bg/60 border-brand-border text-brand-text-muted hover:text-brand-text'
            }`}
          >
            📋 Collect Orders
          </button>
          <button
            disabled={!shopId}
            onClick={() => setActiveTab('import')}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm font-semibold transition-all border cursor-pointer disabled:opacity-40 ${
              activeTab === 'import'
                ? 'bg-indigo-600 text-white border-indigo-500 shadow-md'
                : 'bg-brand-card hover:bg-brand-bg/60 border-brand-border text-brand-text-muted hover:text-brand-text'
            }`}
          >
            📤 CSV / Excel Import
          </button>
        </div>

        {/* Content Pane (9 cols) */}
        <div className="md:col-span-9 bg-brand-card border border-brand-border rounded-3xl p-6 min-h-[500px]">
          
          {/* A. Profile Tab */}
          {activeTab === 'profile' && (
            <form onSubmit={handleProfileSubmit} className="space-y-6">
              <h3 className="text-lg font-bold text-white mb-4">Edit Shop Details</h3>
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs text-slate-400 font-semibold mb-1">Shop Name</label>
                  <input
                    type="text"
                    required
                    value={shopName}
                    onChange={(e) => setShopName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs text-slate-400 font-semibold mb-1">Phone Number</label>
                  <input
                    type="text"
                    value={shopPhone}
                    onChange={(e) => setShopPhone(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs text-slate-400 font-semibold mb-1">Description</label>
                <textarea
                  value={shopDesc}
                  onChange={(e) => setShopDesc(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white h-24"
                ></textarea>
              </div>

              <div>
                <label className="block text-xs text-slate-400 font-semibold mb-1">Street Address</label>
                <input
                  type="text"
                  required
                  value={shopAddress}
                  onChange={(e) => setShopAddress(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                />
              </div>

              <div className="grid grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs text-slate-400 font-semibold mb-1">Latitude (coordinates)</label>
                  <input
                    type="number"
                    step="0.0001"
                    required
                    value={shopLat}
                    onChange={(e) => setShopLat(parseFloat(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs text-slate-400 font-semibold mb-1">Longitude (coordinates)</label>
                  <input
                    type="number"
                    step="0.0001"
                    required
                    value={shopLon}
                    onChange={(e) => setShopLon(parseFloat(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold rounded-lg text-sm shadow-md transition-all"
              >
                Save Profile Configuration
              </button>
            </form>
          )}

          {/* B. Products Tab */}
          {activeTab === 'products' && (
            <div className="space-y-6">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-bold text-white">Products Catalog</h3>
                <button
                  onClick={() => setShowProductModal(true)}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold"
                >
                  + Add Product
                </button>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-400">
                  <thead className="bg-slate-950 text-slate-500 uppercase tracking-wider">
                    <tr>
                      <th className="px-4 py-3">Name</th>
                      <th className="px-4 py-3">SKU</th>
                      <th className="px-4 py-3">Price</th>
                      <th className="px-4 py-3">Availability</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-850">
                    {products.map(p => (
                      <tr key={p.id} className="hover:bg-slate-950/20">
                        <td className="px-4 py-3 font-semibold text-white">{p.name}</td>
                        <td className="px-4 py-3">{p.sku}</td>
                        <td className="px-4 py-3 text-indigo-400">${p.price}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded text-[10px] ${p.isAvailable ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'}`}>
                            {p.isAvailable ? 'In Stock' : 'Out of Stock'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Stock Inventory Tab */}
          {activeTab === 'inventory' && (
            <div className="space-y-6">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-bold text-white">Stock &amp; Inventory Management</h3>
                <button
                  onClick={fetchInventory}
                  className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-xs font-semibold"
                >
                  🔄 Refresh
                </button>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-400">
                  <thead className="bg-slate-950 text-slate-500 uppercase tracking-wider">
                    <tr>
                      <th className="px-4 py-3">Product Name</th>
                      <th className="px-4 py-3">SKU</th>
                      <th className="px-4 py-3 text-center">Available Stock</th>
                      <th className="px-4 py-3 text-center">Reserved Quantity</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-850">
                    {products.map(prod => {
                      const item = inventory.find(i => i.productId === prod.id) || { stockQty: 0, reservedQty: 0 };
                      return (
                        <tr key={prod.id} className="hover:bg-slate-950/20">
                          <td className="px-4 py-3 font-semibold text-white">{prod.name}</td>
                          <td className="px-4 py-3">{prod.sku}</td>
                          <td className="px-4 py-3 text-center font-bold text-indigo-400">{item.stockQty}</td>
                          <td className="px-4 py-3 text-center text-amber-500">{item.reservedQty}</td>
                          <td className="px-4 py-3 text-right">
                            <button
                              onClick={() => {
                                setStockInProductId(prod.id);
                                setStockInQty('');
                                setShowStockInModal(true);
                              }}
                              className="px-2.5 py-1 bg-indigo-600/10 border border-indigo-500/20 hover:bg-indigo-600 text-white text-[10px] font-bold rounded transition-all"
                            >
                              + Stock-In
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* C. Collect Orders Tab */}
          {activeTab === 'orders' && (
            <div className="space-y-6">
              <h3 className="text-lg font-bold text-white">Pending Customer Pickups</h3>

              <div className="space-y-4">
                {orders.map(order => (
                  <div key={order.id} className="bg-slate-950 border border-slate-850 rounded-xl p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                    <div>
                      <div className="flex items-center gap-3">
                        <span className="text-white font-bold text-sm">Order #{order.id.substring(0, 8)}</span>
                        <span className={`px-2 py-0.5 rounded text-[9px] font-extrabold ${
                          order.status === 'READY_FOR_PICKUP' ? 'bg-emerald-500/10 text-emerald-400' :
                          order.status === 'PREPARING' ? 'bg-amber-500/10 text-amber-400' : 'bg-slate-800 text-slate-500'
                        }`}>{order.status}</span>
                      </div>
                      <span className="text-[10px] text-slate-500 block mt-1">Customer ID: {order.customerId}</span>
                      <span className="text-xs text-indigo-400 block font-semibold mt-2">Amount: ${order.totalAmount}</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => navigate(`/chat?recipientId=${order.customerId}&name=Customer`)}
                        className="px-3 py-1.5 bg-slate-900 border border-slate-800 text-white rounded text-xs hover:bg-slate-800"
                      >
                        💬 Chat
                      </button>

                      {order.status === 'PREPARING' && (
                        <button
                          onClick={() => handleMarkReady(order.id)}
                          className="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded text-xs font-semibold"
                        >
                          Mark Ready
                        </button>
                      )}
                      
                      {order.status === 'READY_FOR_PICKUP' && (
                        <button
                          onClick={() => {
                            setOtpTargetOrderId(order.id);
                            setShowOtpModal(true);
                          }}
                          className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold shadow shadow-emerald-500/10"
                        >
                          Verify pickup OTP
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* D. Excel / CSV Import Tab */}
          {activeTab === 'import' && (
            <div className="space-y-6">
              <h3 className="text-lg font-bold text-white">Bulk Products Upload</h3>
              <p className="text-xs text-slate-400">
                Upload a CSV or Excel file containing products catalog. Columns required: <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-400 font-semibold text-[10px]">Name, Description, SKU, Price, CategoryName</code>
              </p>

              <form onSubmit={handleBulkImportSubmit} className="space-y-4 max-w-md">
                <div className="border border-dashed border-slate-800 rounded-xl p-8 flex flex-col items-center justify-center bg-slate-950/20">
                  <span className="text-3xl mb-2">📁</span>
                  <input
                    type="file"
                    accept=".csv, .xlsx"
                    onChange={(e) => setImportFile(e.target.files ? e.target.files[0] : null)}
                    className="text-xs text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-xs file:font-semibold file:bg-indigo-600 file:text-white hover:file:bg-indigo-500 cursor-pointer"
                  />
                </div>
                
                <button
                  type="submit"
                  disabled={!importFile}
                  className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 text-white font-semibold rounded-lg text-xs"
                >
                  Start Import Execution
                </button>
              </form>

              {activeJob && (
                <div className="bg-slate-950 rounded-xl border border-slate-850 p-5 mt-6 space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-xs font-bold text-white">Import Job Details</span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      activeJob.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400' :
                      activeJob.status === 'PROCESSING' ? 'bg-indigo-500/10 text-indigo-400' : 'bg-red-500/10 text-red-400'
                    }`}>{activeJob.status}</span>
                  </div>
                  <div className="grid grid-cols-3 gap-4 text-center text-xs">
                    <div className="bg-slate-900 p-3 rounded">
                      <span className="text-slate-500 block">Total Checked</span>
                      <span className="text-lg font-bold text-white">{activeJob.totalRecords}</span>
                    </div>
                    <div className="bg-slate-900 p-3 rounded">
                      <span className="text-slate-500 block">Processed</span>
                      <span className="text-lg font-bold text-emerald-400">{activeJob.processedRecords}</span>
                    </div>
                    <div className="bg-slate-900 p-3 rounded">
                      <span className="text-slate-500 block">Errors</span>
                      <span className="text-lg font-bold text-red-400">{activeJob.failedRecords}</span>
                    </div>
                  </div>
                  {activeJob.errorLog && (
                    <div className="bg-red-500/5 border border-red-500/10 rounded p-3">
                      <span className="text-[10px] text-red-400 block font-semibold mb-1">System Error Logs</span>
                      <pre className="text-[10px] text-slate-500 max-h-32 overflow-y-auto whitespace-pre-wrap">{activeJob.errorLog}</pre>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

        </div>
      </div>

      {/* Manual Product Creation Modal */}
      {showProductModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <form onSubmit={handleCreateProduct} className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h3 className="text-lg font-bold text-white">Add Catalog Item</h3>
            
            <div>
              <label className="block text-xs text-slate-400 font-semibold mb-1">Product Name</label>
              <input
                type="text"
                required
                value={newProductName}
                onChange={(e) => setNewProductName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-1.5 text-xs text-white"
              />
            </div>

            <div>
              <label className="block text-xs text-slate-400 font-semibold mb-1">Description</label>
              <textarea
                value={newProductDesc}
                onChange={(e) => setNewProductDesc(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-1.5 text-xs text-white h-16"
              ></textarea>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs text-slate-400 font-semibold mb-1">SKU</label>
                <input
                  type="text"
                  required
                  value={newProductSku}
                  onChange={(e) => setNewProductSku(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-1.5 text-xs text-white"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 font-semibold mb-1">Price ($)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={newProductPrice}
                  onChange={(e) => setNewProductPrice(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-1.5 text-xs text-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs text-slate-400 font-semibold mb-1">Category Name</label>
              <input
                type="text"
                value={newProductCategory}
                onChange={(e) => setNewProductCategory(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-1.5 text-xs text-white"
              />
            </div>

            <div className="flex gap-4 pt-2">
              <button
                type="submit"
                className="flex-1 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded font-semibold text-xs"
              >
                Create
              </button>
              <button
                type="button"
                onClick={() => setShowProductModal(false)}
                className="flex-1 py-2.5 bg-slate-950 border border-slate-800 text-white rounded font-semibold text-xs"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* OTP Pickup Code Verification Modal */}
      {showOtpModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <form onSubmit={handleVerifyOtp} className="w-full max-w-sm bg-slate-900 border border-slate-800 rounded-2xl p-6 text-center space-y-4">
            <span className="text-4xl block">🔑</span>
            <h3 className="text-lg font-bold text-white">Collect Pick Up OTP</h3>
            <p className="text-xs text-slate-400">Ask the customer for the 6-digit OTP code shown on their receipt.</p>
            
            <input
              type="text"
              required
              maxLength={6}
              placeholder="000000"
              value={otpCode}
              onChange={(e) => setOtpCode(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg py-3 text-center text-2xl font-extrabold tracking-widest text-white focus:outline-none focus:border-indigo-500"
            />

            <div className="flex gap-4 pt-2">
              <button
                type="submit"
                className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded font-semibold text-xs"
              >
                Verify Code
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowOtpModal(false);
                  setOtpCode('');
                }}
                className="flex-1 py-2.5 bg-slate-950 border border-slate-800 text-white rounded font-semibold text-xs"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Manual Stock-In Modal */}
      {showStockInModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <form onSubmit={handleStockIn} className="w-full max-w-sm bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h3 className="text-lg font-bold text-white">Stock-In Inventory</h3>
            <p className="text-xs text-slate-400">
              Specify the positive quantity to add to the existing stock for: <strong className="text-white">{products.find(p => p.id === stockInProductId)?.name}</strong>
            </p>
            
            <div>
              <label className="block text-xs text-slate-400 font-semibold mb-1">Quantity to Add</label>
              <input
                type="number"
                required
                min={1}
                value={stockInQty}
                onChange={(e) => setStockInQty(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-sm text-white"
                placeholder="E.g. 50"
              />
            </div>

            <div className="flex gap-4 pt-2">
              <button
                type="submit"
                className="flex-1 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded font-semibold text-xs transition-colors"
              >
                Add Stock
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowStockInModal(false);
                  setStockInProductId(null);
                  setStockInQty('');
                }}
                className="flex-1 py-2.5 bg-slate-950 border border-slate-800 text-white rounded font-semibold text-xs transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
      {/* Settings Modal */}
      {showSettings && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-xl bg-brand-bg border border-brand-border rounded-3xl p-6 shadow-2xl overflow-y-auto max-h-[90vh] animate-fadeIn text-brand-text">
            <SettingsPanel onClose={() => setShowSettings(false)} />
          </div>
        </div>
      )}
    </div>
  );
};

export default ShopDashboard;
