import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import axiosClient from '../api/axiosClient';

export interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

interface AuthState {
  user: User | null;
  shopId: string | null;
  loading: boolean;
  error: string | null;
}

const getStoredUser = (): User | null => {
  try {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
};

const getStoredShopId = (): string | null => {
  return localStorage.getItem('shopId');
};

const initialState: AuthState = {
  user: getStoredUser(),
  shopId: getStoredShopId(),
  loading: false,
  error: null,
};

// Auxiliary Thunk to fetch user's shop profile if they are a Shop Owner
export const fetchShopProfileThunk = createAsyncThunk(
  'auth/fetchShopProfile',
  async (payload: { userId: string; token: string }) => {
    try {
      const res = await axiosClient.get('/shops/nearby?longitude=0&latitude=0&radius=999999999', {
        headers: { Authorization: `Bearer ${payload.token}` }
      });
      const myShop = res.data.find((s: any) => s.ownerId === payload.userId);
      if (myShop) {
        localStorage.setItem('shopId', myShop.id);
        return myShop.id;
      }
      return null;
    } catch (err) {
      console.error("Failed to query owned shop profile", err);
      return null;
    }
  }
);

// Helper to construct user data and persist credentials
const handleAuthPayload = (data: any, dispatch: any): User => {
  const userData: User = {
    userId: data.userId,
    email: data.email,
    firstName: data.firstName,
    lastName: data.lastName,
    roles: Array.isArray(data.roles) ? data.roles : [data.roles],
  };
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('user', JSON.stringify(userData));

  if (userData.roles.includes('ROLE_SHOP_OWNER')) {
    dispatch(fetchShopProfileThunk({ userId: userData.userId, token: data.accessToken }));
  }

  return userData;
};

export const loginThunk = createAsyncThunk(
  'auth/login',
  async (payload: { email: string; password: string }, { rejectWithValue, dispatch }) => {
    try {
      const res = await axiosClient.post('/auth/login', payload);
      return handleAuthPayload(res.data, dispatch);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || err.message || 'Authentication failed');
    }
  }
);

export const registerThunk = createAsyncThunk(
  'auth/register',
  async (payload: any, { rejectWithValue, dispatch }) => {
    try {
      const res = await axiosClient.post('/auth/register', payload);
      if (res.data && res.data.accessToken) {
        return handleAuthPayload(res.data, dispatch);
      }
      return null;
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || err.message || 'Registration failed');
    }
  }
);

export const loginGoogleThunk = createAsyncThunk(
  'auth/google',
  async (email: string, { rejectWithValue, dispatch }) => {
    try {
      const res = await axiosClient.post('/auth/google', { idToken: email });
      return handleAuthPayload(res.data, dispatch);
    } catch (err: any) {
      return rejectWithValue(err.message || 'Google Auth simulation failed');
    }
  }
);

export const initiateRegisterThunk = createAsyncThunk(
  'auth/registerInitiate',
  async (payload: { email: string; password: string }, { rejectWithValue, dispatch }) => {
    try {
      const res = await axiosClient.post('/auth/register/initiate', payload);
      return handleAuthPayload(res.data, dispatch);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || err.message || 'Initiation failed');
    }
  }
);

export const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    initializeAuth: (state) => {
      const storedUser = localStorage.getItem('user');
      const storedShopId = localStorage.getItem('shopId');
      if (storedUser) {
        state.user = JSON.parse(storedUser);
      }
      if (storedShopId) {
        state.shopId = storedShopId;
      }
    },
    logout: (state) => {
      state.user = null;
      state.shopId = null;
      state.error = null;
      localStorage.clear();
    },
    setShopId: (state, action: PayloadAction<string | null>) => {
      state.shopId = action.payload;
      if (action.payload) {
        localStorage.setItem('shopId', action.payload);
      } else {
        localStorage.removeItem('shopId');
      }
    },
    updateUser: (state, action: PayloadAction<Partial<User>>) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
        localStorage.setItem('user', JSON.stringify(state.user));
      }
    },
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(loginThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginThunk.fulfilled, (state, action: PayloadAction<User>) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(loginThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Register
      .addCase(registerThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(registerThunk.fulfilled, (state, action: PayloadAction<User | null>) => {
        state.loading = false;
        if (action.payload) {
          state.user = action.payload;
        }
      })
      .addCase(registerThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Google
      .addCase(loginGoogleThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginGoogleThunk.fulfilled, (state, action: PayloadAction<User>) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(loginGoogleThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Register Initiate
      .addCase(initiateRegisterThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(initiateRegisterThunk.fulfilled, (state, action: PayloadAction<User>) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(initiateRegisterThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Fetch shop profile
      .addCase(fetchShopProfileThunk.fulfilled, (state, action: PayloadAction<string | null>) => {
        state.shopId = action.payload;
      });
  },
});

export const { initializeAuth, logout, setShopId, updateUser, clearError } = authSlice.actions;
export default authSlice.reducer;
