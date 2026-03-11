import { useEffect, useState } from "react";
import { Box, Card, CardContent, Typography, Grid, CircularProgress } from "@mui/material";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

export default function Analytics() {
  const { companyId } = useAuth();
  const [sales, setSales] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/analytics/sales-summary?companyId=${companyId}`)
      .then((r) => setSales(r.data))
      .catch(() => setSales({ totalOrders: 0, totalRevenue: 0 }))
      .finally(() => setLoading(false));
  }, [companyId]);

  if (loading) return <Box display="flex" justifyContent="center" mt={10}><CircularProgress /></Box>;

  const chartData = [
    { name: "Pedidos", valor: sales?.totalOrders || 0 },
    { name: "Receita (R$ mil)", valor: Math.round((sales?.totalRevenue || 0) / 1000) },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" mb={3}>Analytics</Typography>
      <Grid container spacing={3} mb={4}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Card sx={{ background: "linear-gradient(135deg, #1976d2, #1565c0)", color: "#fff" }}>
            <CardContent>
              <Typography variant="subtitle2">Total de Pedidos</Typography>
              <Typography variant="h3" fontWeight="bold">{sales?.totalOrders ?? 0}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Card sx={{ background: "linear-gradient(135deg, #2e7d32, #1b5e20)", color: "#fff" }}>
            <CardContent>
              <Typography variant="subtitle2">Faturamento Total</Typography>
              <Typography variant="h3" fontWeight="bold">R$ {(sales?.totalRevenue ?? 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <Card elevation={0} sx={{ border: "1px solid #e0e0e0", p: 2 }}>
        <Typography variant="h6" mb={2}>Resumo</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="valor" fill="#1976d2" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </Box>
  );
}
