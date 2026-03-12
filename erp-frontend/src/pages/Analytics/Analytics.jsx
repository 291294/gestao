import { useEffect, useState } from "react";
import { Box, Card, CardContent, Typography, Grid, CircularProgress, TextField } from "@mui/material";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line,
} from "recharts";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

const COLORS = ["#1976d2", "#2e7d32", "#ed6c02", "#d32f2f", "#9c27b0", "#0288d1", "#558b2f"];

export default function Analytics() {
  const { companyId } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [sales, setSales] = useState(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState("month");

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.get(`/analytics/dashboard?companyId=${companyId}`).catch(() => ({ data: null })),
      api.get(`/analytics/sales-summary?companyId=${companyId}`).catch(() => ({ data: null })),
    ]).then(([d, s]) => {
      setDashboard(d.data);
      setSales(s.data);
    }).finally(() => setLoading(false));
  }, [companyId]);

  if (loading) return <Box display="flex" justifyContent="center" mt={10}><CircularProgress /></Box>;

  const stats = dashboard?.stats || {};
  const revenueByMonth = dashboard?.revenueByMonth || [];
  const ordersByStatus = dashboard?.ordersByStatus || [];

  const kpis = [
    { label: "Total Pedidos", value: stats.totalOrders ?? sales?.totalOrders ?? 0, color: "#1976d2" },
    { label: "Total Clientes", value: stats.totalClients ?? 0, color: "#7b1fa2" },
    { label: "Receita Total", value: `R$ ${(stats.totalRevenue ?? sales?.totalRevenue ?? 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`, color: "#2e7d32" },
    { label: "Itens Baixo Estoque", value: stats.lowStockItems ?? 0, color: "#ed6c02" },
    { label: "Produção Ativa", value: stats.activeProduction ?? 0, color: "#0288d1" },
    { label: "Total Faturas", value: stats.totalInvoices ?? 0, color: "#388e3c" },
    { label: "Faturas Vencidas", value: stats.overdueInvoices ?? 0, color: "#d32f2f" },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Analytics</Typography>
      </Box>

      {/* KPI Cards */}
      <Grid container spacing={2} mb={4}>
        {kpis.map((kpi, i) => (
          <Grid key={i} size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ background: `linear-gradient(135deg, ${kpi.color}, ${kpi.color}dd)`, color: "#fff" }}>
              <CardContent>
                <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>{kpi.label}</Typography>
                <Typography variant="h4" fontWeight="bold">{kpi.value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Receita Mensal */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2 }}>
            <Typography variant="h6" mb={2}>Receita por Mês</Typography>
            <ResponsiveContainer width="100%" height={320}>
              <BarChart data={revenueByMonth.length ? revenueByMonth : [{ month: "-", revenue: 0 }]}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis tickFormatter={(v) => `R$${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v) => [`R$ ${Number(v).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`, "Receita"]} />
                <Bar dataKey="revenue" fill="#1976d2" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Grid>

        {/* Pedidos por Status */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2 }}>
            <Typography variant="h6" mb={2}>Pedidos por Status</Typography>
            <ResponsiveContainer width="100%" height={320}>
              <PieChart>
                <Pie data={ordersByStatus.length ? ordersByStatus : [{ status: "Sem dados", count: 1 }]} dataKey="count" nameKey="status" cx="50%" cy="50%" outerRadius={100} label>
                  {ordersByStatus.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Grid>

        {/* Gráfico de Tendência */}
        <Grid size={{ xs: 12 }}>
          <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2 }}>
            <Typography variant="h6" mb={2}>Tendência de Receita</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={revenueByMonth.length ? revenueByMonth : [{ month: "-", revenue: 0 }]}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis tickFormatter={(v) => `R$${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v) => [`R$ ${Number(v).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`, "Receita"]} />
                <Line type="monotone" dataKey="revenue" stroke="#2e7d32" strokeWidth={3} dot={{ r: 5 }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
