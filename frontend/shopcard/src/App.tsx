import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store, useAppSelector } from './store';
import { ThemeProvider } from './context/ThemeContext';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Onboarding from './pages/Onboarding';
import UserProfileView from './pages/UserProfileView';
import CustomerDashboard from './pages/CustomerDashboard';
import ShopDashboard from './pages/ShopDashboard';
import ChatWindow from './pages/ChatWindow';
import VerifyEmail from './pages/VerifyEmail';
import ResetPassword from './pages/ResetPassword';

const ProtectedRoute: React.FC<{ children: React.ReactNode; allowedRole?: string; skipOnboardingCheck?: boolean }> = ({ 
  children, 
  allowedRole,
  skipOnboardingCheck = false
}) => {
  const { user, loading } = useAppSelector((state) => state.auth);

  if (loading) {
    return (
      <div className="min-h-screen bg-brand-bg text-brand-text flex items-center justify-center text-xs">
        Loading Authentication...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const isOnlyUser = user.roles.length === 1 && user.roles.includes('ROLE_USER');

  // Force onboarding if they only have ROLE_USER
  if (isOnlyUser && !skipOnboardingCheck) {
    return <Navigate to="/onboarding" replace />;
  }

  // Prevent onboarded users from returning to onboarding
  if (!isOnlyUser && skipOnboardingCheck) {
    return <Navigate to="/" replace />;
  }

  if (allowedRole && !user.roles.includes(allowedRole)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

const HomeRedirect: React.FC = () => {
  const { user } = useAppSelector((state) => state.auth);
  if (!user) return <Navigate to="/login" replace />;
  
  if (user.roles.length === 1 && user.roles.includes('ROLE_USER')) {
    return <Navigate to="/onboarding" replace />;
  }

  if (user.roles.includes('ROLE_SHOP_OWNER')) {
    return <Navigate to="/owner" replace />;
  }
  return <CustomerDashboard />;
};

function App() {
  return (
    <Provider store={store}>
      <ThemeProvider>
        <Router>
          <Routes>
            {/* Public Auth Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/verify-email" element={<VerifyEmail />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            {/* Public User Profile View */}
            <Route path="/u/:username" element={<UserProfileView />} />

            {/* Onboarding Flow (Authenticated but needs role choice) */}
            <Route
              path="/onboarding"
              element={
                <ProtectedRoute skipOnboardingCheck={true}>
                  <Onboarding />
                </ProtectedRoute>
              }
            />

            {/* Main Application Routes */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <HomeRedirect />
                </ProtectedRoute>
              }
            />

            <Route
              path="/owner"
              element={
                <ProtectedRoute allowedRole="ROLE_SHOP_OWNER">
                  <ShopDashboard />
                </ProtectedRoute>
              }
            />

            <Route
              path="/chat"
              element={
                <ProtectedRoute>
                  <ChatWindow />
                </ProtectedRoute>
              }
            />

            {/* Catch-all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </ThemeProvider>
    </Provider>
  );
}

export default App;
