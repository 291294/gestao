import { useEffect, useState } from "react";
import { Box, Typography, Grid, Card, CardContent, CircularProgress, Chip } from "@mui/material";
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from "recharts";
import {
  ShoppingCart, People, Inventory, AttachMoney,
  Warning, Factory, TrendingUp,
} from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

const COLORS = ["#1976d2", "#2e7d32", "#ed6c02", "#d32f2f", "#9c27b0", "#0288d1"];

function StatCard({ title, value, subtitle, icon, gradient }) {
  return (
    <Card sx={{ background: gradient, color: "#fff", height: "100%" }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>{title}</Typography>
            <Typography variant="h4" fontWeight="bold" mt={0.5}>{value}</Typography>
            {subtitle && <Typography variant="caption" sx={{ opacity: 0.8 }}>{subtitle}</Typography>}
          </Box>
          <Box sx={{ opacity: 0.3, fontSize: 48 }}>{icon}</Box>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const { companyId, user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ orders: 0, clients: 0, revenue: 0, lowStock: 0, production: 0, invoices: 0 });
  const [revenueChart, setRevenueChart] = useState([]);
  const [ordersByStatus, setOrdersByStatus] = useState([]);
  const [stockAlerts, setStockAlerts] = useState([]);

  useEffect(() => {
    const load = async () => {
      try {
        const [dashRes, inventoryRes] = await Promise.allSettled([
          api.get(`/analytics/dashboard?companyId=${companyId}`),
          api.get(`/inventory/company/${companyId}`),
        ]);

        const dash = dashRes.status === "fulfilled" ? dashRes.value.data : {};
        const inventory = inventoryRes.status === "fulfilled" ? (inventoryRes.value.data || []) : [];

        const s = dash.stats || {};
        setStats({
          orders: s.totalOrders || 0,
          clients: s.totalClients || 0,
          revenue: s.totalRevenue || 0,
          lowStock: s.lowStockItems || 0,
          production: s.activeProduction || 0,
          invoices: s.totalInvoices || 0,
        });

        setRevenueChart(
          (dash.revenueByMonth || []).map((r) => ({ mes: r.month, receita: r.revenue }))
        );

        setOrdersByStatus(
          (dash.ordersByStatus || []).map((o) => ({ name: o.status, value: o.count }))
        );

        const lowStockItems = inventory.filter((i) => (i.quantityOnHand || 0) <= (i.minStockLevel || 0));
        setStockAlerts(lowStockItems.slice(0, 5));
      } catch { /* ignore */ }
      finally { setLoading(false); }
    };
    load();
  }, [companyId]);

  if (loading) return <Box display="flex" justifyContent="center" mt={10}><CircularProgress size={60} /></Box>;

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" mb={1}>Dashboard</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>Bem-vindo, {user?.fullName || user?.username || "Administrador"}</Typography>

      {/* KPI Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Pedidos" value={stats.orders} icon={<ShoppingCart sx={{ fontSize: 48 }} />} gradient="linear-gradient(135deg, #1976d2, #1565c0)" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Clientes" value={stats.clients} icon={<People sx={{ fontSize: 48 }} />} gradient="linear-gradient(135deg, #7b1fa2, #6a1b9a)" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Faturamento" value={`R$ ${(stats.revenue/1000).toFixed(0)}k`} icon={<AttachMoney sx={{ fontSize: 48 }} />} gradient="linear-gradient(135deg, #2e7d32, #1b5e20)" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Estoque Baixo" value={stats.lowStock} icon={<Warning sx={{ fontSize: 48 }} />} gradient={stats.lowStock > 0 ? "linear-gradient(135deg, #d32f2f, #c62828)" : "linear-gradient(135deg, #757575, #616161)"} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Produção Ativa" value={stats.production} icon={<Factory sx={{ fontSize: 48 }} />} gradient="linear-gradient(135deg, #ed6c02, #e65100)" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
          <StatCard title="Faturas" value={stats.invoices} icon={<TrendingUp sx={{ fontSize: 48 }} />} gradient="linear-gradient(135deg, #0288d1, #01579b)" />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} mb={4}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2, height: "100%" }}>
            <Typography variant="h6" mb={2}>Receita por Mês</Typography>
            {revenueChart.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={revenueChart}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="mes" />
                  <YAxis />
                  <Tooltip formatter={(v) => `R$ ${v.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`} />
                  <Bar dataKey="receita" fill="#1976d2" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : <Typography color="text.secondary" textAlign="center" py={8}>Sem dados de receita</Typography>}
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2, height: "100%" }}>
            <Typography variant="h6" mb={2}>Pedidos por Status</Typography>
            {ordersByStatus.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie data={ordersByStatus} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label>
                    {ordersByStatus.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : <Typography color="text.secondary" textAlign="center" py={8}>Sem dados</Typography>}
          </Card>
        </Grid>
      </Grid>

      {/* Alerts */}
      {stockAlerts.length > 0 && (
        <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2 }}>
          <Typography variant="h6" mb={2} display="flex" alignItems="center" gap={1}>
            <Warning color="error" /> Alertas de Estoque Baixo
          </Typography>
          <Grid container spacing={2}>
            {stockAlerts.map((item) => (
              <Grid key={item.id} size={{ xs: 12, sm: 6, md: 4 }}>
                <Card variant="outlined" sx={{ borderColor: "#d32f2f" }}>
                  <CardContent>
                    <Typography fontWeight="bold">{item.productName || `Produto #${item.productId}`}</Typography>
                    <Typography variant="body2" color="text.secondary">Local: {item.warehouseLocation || "N/A"}</Typography>
                    <Box display="flex" gap={1} mt={1}>
                      <Chip label={`Atual: ${item.quantityOnHand}`} color="error" size="small" />
                      <Chip label={`Mínimo: ${item.minStockLevel}`} size="small" variant="outlined" />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Card>
      )}
    </Box>
  );
}
