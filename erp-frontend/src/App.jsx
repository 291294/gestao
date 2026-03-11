import { CssBaseline, ThemeProvider, createTheme, Toolbar } from "@mui/material";
import { Box } from "@mui/material";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import Sidebar, { DRAWER_WIDTH } from "./components/Sidebar";
import Topbar from "./components/Topbar";
import Login from "./pages/Login/Login";
import Dashboard from "./pages/Dashboard/Dashboard";
import Clients from "./pages/Clients/Clients";
import Quotes from "./pages/Quotes/Quotes";
import Orders from "./pages/Orders/Orders";
import Inventory from "./pages/Inventory/Inventory";
import Warehouses from "./pages/Warehouses/Warehouses";
import Manufacturing from "./pages/Manufacturing/Manufacturing";
import Invoicing from "./pages/Invoicing/Invoicing";
import Payments from "./pages/Payments/Payments";
import Notifications from "./pages/Notifications/Notifications";
import Analytics from "./pages/Analytics/Analytics";

const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#1976d2" },
    secondary: { main: "#2e7d32" },
    background: { default: "#f5f5f5" },
  },
  typography: {
    fontFamily: "'Roboto', 'Helvetica', 'Arial', sans-serif",
  },
});

function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return null;
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function AppRoutes() {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return null;

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/" replace /> : <Login />}
      />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Box sx={{ display: "flex" }}>
              <Sidebar />
              <Topbar />
              <Box
                component="main"
                sx={{
                  flexGrow: 1,
                  ml: `${DRAWER_WIDTH}px`,
                  p: 3,
                  minHeight: "100vh",
                  backgroundColor: "background.default",
                }}
              >
                <Toolbar />
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/clientes" element={<Clients />} />
                  <Route path="/orcamentos" element={<Quotes />} />
                  <Route path="/pedidos" element={<Orders />} />
                  <Route path="/estoque" element={<Inventory />} />
                  <Route path="/armazens" element={<Warehouses />} />
                  <Route path="/producao" element={<Manufacturing />} />
                  <Route path="/faturamento" element={<Invoicing />} />
                  <Route path="/pagamentos" element={<Payments />} />
                  <Route path="/notificacoes" element={<Notifications />} />
                  <Route path="/analytics" element={<Analytics />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </Box>
            </Box>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
