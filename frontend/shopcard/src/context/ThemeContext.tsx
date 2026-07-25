import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import { useAppSelector } from '../store';

export type Theme = 'light' | 'dark' | 'emerald' | 'cosmic' | 'sunset';

interface ThemeDescription {
  id: Theme;
  name: string;
  icon: string;
}

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  availableThemes: ThemeDescription[];
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const availableThemes: ThemeDescription[] = [
  { id: 'light', name: 'Light Mode', icon: '☀️' },
  { id: 'dark', name: 'Dark Mode', icon: '🌙' },
  { id: 'emerald', name: 'Emerald Garden', icon: '🌿' },
  { id: 'cosmic', name: 'Cosmic Purple', icon: '🌌' },
  { id: 'sunset', name: 'Sunset Warmth', icon: '🌅' }
];

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAppSelector((state) => state.auth);
  const [theme, setThemeState] = useState<Theme>(() => {
    const saved = localStorage.getItem('theme') as Theme;
    return saved || 'light';
  });

  // Apply theme to document element
  useEffect(() => {
    const root = document.documentElement;
    
    // Remove previous theme classes & attributes
    availableThemes.forEach(t => {
      root.classList.remove(t.id);
    });
    root.classList.remove('dark'); // standard tailwind class
    
    // Set new theme
    root.setAttribute('data-theme', theme);
    root.classList.add(theme);
    
    if (theme === 'dark') {
      root.classList.add('dark');
    }
  }, [theme]);

  // Sync theme from database profile-service when user logs in
  useEffect(() => {
    if (user) {
      axiosClient.get('/profiles/my')
        .then(res => {
          if (res.data && res.data.theme) {
            const dbTheme = res.data.theme as Theme;
            if (dbTheme !== theme) {
              setThemeState(dbTheme);
              localStorage.setItem('theme', dbTheme);
            }
          }
        })
        .catch(err => {
          console.warn("Could not load user theme settings from profile-service:", err);
        });
    }
  }, [user]);

  const setTheme = async (newTheme: Theme) => {
    setThemeState(newTheme);
    localStorage.setItem('theme', newTheme);

    // Persist to backend profile-service settings if logged in
    if (user) {
      try {
        await axiosClient.put('/profiles/my/settings', { theme: newTheme });
      } catch (err) {
        console.warn("Failed to persist theme settings to profile-service:", err);
      }
    }
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, availableThemes }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
};
