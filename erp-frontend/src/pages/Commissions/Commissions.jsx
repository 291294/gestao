import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip, Card, CardContent, Grid,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, ThumbUp, Payment, Cancel as CancelIcon } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const statusColor = { PENDING: "warning", APPROVED: "info", PAID: "success", CANCELLED: "error" };
const statusLabel = { PENDING: "Pendente", APPROVED: "Aprovada", PAID: "Paga", CANCELLED: "Cancelada" };

export default function Commissions() {
  const { companyId, checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ sellerId: "", orderId: "", amount: "", percentage: "" });
  const [saving, setSaving] = useState(false);
  const [totals, setTotals] = useState({ paid: 0, pending: 0 });
  const { showSuccess, showError } = useSnackbar();

  const canApprove = checkPermission("commission", "approve");

  const fetchData = useCallback(() => {
    setLoading(true);
    // Busca comissões por status
    Promise.all([
      api.get("/commissions/status/PENDING").catch(() => ({ data: [] })),
      api.get("/commissions/status/APPROVED").catch(() => ({ data: [] })),
      api.get("/commissions/status/PAID").catch(() => ({ data: [] })),
    ]).then(([p, a, pd]) => {
      const all = [...(p.data || []), ...(a.data || []), ...(pd.data || [])];
      const unique = Array.from(new Map(all.map(i => [i.id, i])).values());
      setRows(unique);
    }).catch(() => setRows([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleApprove = async (id) => {
    try { await api.post(`/commissions/${id}/approve`); showSuccess("Comissão aprovada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao aprovar"); }
  };

  const handlePay = async (id) => {
    try { await api.post(`/commissions/${id}/pay`); showSuccess("Comissão paga com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao pagar"); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancelar esta comissão?")) return;
    try { await api.post(`/commissions/${id}/cancel`); showSuccess("Comissão cancelada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao cancelar"); }
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      await api.post("/commissions", {
        sellerId: Number(form.sellerId),
        orderId: Number(form.orderId),
        amount: Number(form.amount),
        percentage: Number(form.percentage),
        companyId,
      });
      setOpen(false);
      showSuccess("Comissão criada com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao criar"); }
    finally { setSaving(false); }
  };

  const pendingTotal = rows.filter(r => r.status === "PENDING").reduce((s, r) => s + (r.amount || 0), 0);
  const paidTotal = rows.filter(r => r.status === "PAID").reduce((s, r) => s + (r.amount || 0), 0);

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "sellerId", headerName: "Vendedor ID", width: 120 },
    { field: "orderId", headerName: "Pedido", width: 100 },
    {
      field: "amount", headerName: "Valor", width: 140,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    { field: "percentage", headerName: "%", width: 80, valueFormatter: (v) => v != null ? `${v}%` : "—" },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => <Chip label={statusLabel[p.value] || p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 150, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {canApprove && p.row.status === "PENDING" && (
            <Tooltip title="Aprovar"><IconButton size="small" color="primary" onClick={() => handleApprove(p.row.id)}><ThumbUp fontSize="small" /></IconButton></Tooltip>
          )}
          {canApprove && p.row.status === "APPROVED" && (
            <Tooltip title="Pagar"><IconButton size="small" color="success" onClick={() => handlePay(p.row.id)}><Payment fontSize="small" /></IconButton></Tooltip>
          )}
          {p.row.status !== "PAID" && p.row.status !== "CANCELLED" && (
            <Tooltip title="Cancelar"><IconButton size="small" color="error" onClick={() => handleCancel(p.row.id)}><CancelIcon fontSize="small" /></IconButton></Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Comissões</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => {
          setForm({ sellerId: "", orderId: "", amount: "", percentage: "" });
          setOpen(true);
        }}>Nova Comissão</Button>
      </Box>

      <Grid container spacing={2} mb={3}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Card sx={{ background: "linear-gradient(135deg, #ff9800, #f57c00)", color: "#fff" }}>
            <CardContent>
              <Typography variant="subtitle2">Pendentes</Typography>
              <Typography variant="h4" fontWeight="bold">R$ {pendingTotal.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Card sx={{ background: "linear-gradient(135deg, #2e7d32, #1b5e20)", color: "#fff" }}>
            <CardContent>
              <Typography variant="subtitle2">Pagas</Typography>
              <Typography variant="h4" fontWeight="bold">R$ {paidTotal.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box sx={{ height: 500, bgcolor: "#fff", borderRadius: 2 }}>
        <DataGrid rows={rows} columns={columns} loading={loading}
          pageSizeOptions={[10, 25, 50]} initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick slots={{ toolbar: GridToolbar }}
          slotProps={{ toolbar: { showQuickFilter: true, quickFilterProps: { debounceMs: 300 } } }}
          sx={{ border: "1px solid #e0e0e0" }}
        />
      </Box>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Nova Comissão</DialogTitle>
        <DialogContent dividers>
          <TextField label="Vendedor ID" fullWidth margin="normal" type="number" value={form.sellerId} onChange={(e) => setForm({ ...form, sellerId: e.target.value })} required />
          <TextField label="Pedido ID" fullWidth margin="normal" type="number" value={form.orderId} onChange={(e) => setForm({ ...form, orderId: e.target.value })} required />
          <TextField label="Valor (R$)" fullWidth margin="normal" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
          <TextField label="Percentual (%)" fullWidth margin="normal" type="number" value={form.percentage} onChange={(e) => setForm({ ...form, percentage: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>{saving ? "Criando..." : "Criar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
