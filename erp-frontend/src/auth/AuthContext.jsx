import { createContext, useContext, useState, useEffect, useCallback } from "react";
import api from "../api/apiClient";
import { hasRouteAccess, canPerform } from "./permissions";

const AuthContext = createContext(null);

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    const stored = localStorage.getItem("user");
    if (token && stored) {
      try {
        setUser(JSON.parse(stored));
      } catch {
        localStorage.clear();
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    const res = await api.post("/auth/login", { username, password });
    const data = res.data;

    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);

    const userData = {
      username: data.username,
      email: data.email,
      fullName: data.fullName,
      companyId: data.companyId,
      roles: data.roles || [],
      permissions: data.permissions || [],
    };
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);

    return userData;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
  }, []);

  const checkRoute = useCallback(
    (path) => hasRouteAccess(user?.roles || [], path),
    [user]
  );

  const checkPermission = useCallback(
    (resource, action) => canPerform(user?.roles || [], user?.permissions || [], resource, action),
    [user]
  );

  const companyId = user?.companyId || 1;

  const value = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
    companyId,
    checkRoute,
    checkPermission,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
