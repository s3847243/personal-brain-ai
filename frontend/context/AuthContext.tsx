"use client";
import React, { createContext, useContext, useEffect, useRef, useState, ReactNode } from 'react';
import axiosInstance from '@/lib/axiosInstance';

interface User { id: string; email: string; name: string; }

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const didRunRef = useRef(false);

  const isAuthenticated = !!user;

  const checkAuth = async (): Promise<boolean> => {
    try {
      setIsLoading(true);
      const res = await axiosInstance.get('/auth/verify');
      console.log("Auth check response: verify before", res);

      if (res.status === 200) {
        console.log("Auth check response:", res);
        setUser(res.data.user);
        return true;
      }
      setUser(null);
      return false;
    } catch {
      setUser(null);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (didRunRef.current) return;
    didRunRef.current = true;
    void checkAuth();
  }, []);

  const login = async (email: string, password: string) => {
    const res = await axiosInstance.post('/auth/login', { email, password });
    if (res.status === 200) {
      setUser(res.data.user);
    } else {
      throw new Error('Login failed');
    }
  };

  const logout = async () => {
    try { await axiosInstance.post('/auth/logout'); } catch {}
    setUser(null);
  };

  const value: AuthContextType = { user, isLoading, isAuthenticated, login, logout, checkAuth };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
