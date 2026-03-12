import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip, LinearProgress,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, CheckCircle, Cancel as CancelIcon } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const statusColor = { ACTIVE: "info", COMPLETED: "success", CANCELLED: "error", EXPIRED: "warning" };
const statusLabel = { ACTIVE: "Ativa", COMPLETED: "Atingida", CANCELLED: "Cancelada", EXPIRED: "Expirada" };
const targetTypes = ["INDIVIDUAL", "TEAM", "COMPANY"];

export default function SalesTargets() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ sellerId: "", targetType: "INDIVIDUAL", targetAmount: "", startDate: "", endDate: "", description: "" });
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/sales-targets/company/${companyId}`)
      .then((r) => setRows(r.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleComplete = async (id) => {
    try { await api.post(`/sales-targets/${id}/complete`); showSuccess("Meta concluída com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancelar esta meta?")) return;
    try { await api.post(`/sales-targets/${id}/cancel`); showSuccess("Meta cancelada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      await api.post("/sales-targets", {
        sellerId: form.sellerId ? Number(form.sellerId) : null,
        targetType: form.targetType,
        targetAmount: Number(form.targetAmount),
        startDate: form.startDate,
        endDate: form.endDate,
        description: form.description,
        companyId,
      });
      setOpen(false);
      showSuccess("Meta criada com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao criar meta"); }
    finally { setSaving(false); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "description", headerName: "Descrição", flex: 1, minWidth: 200 },
    { field: "targetType", headerName: "Tipo", width: 120 },
    {
      field: "targetAmount", headerName: "Meta", width: 140,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "achievedAmount", headerName: "Alcançado", width: 140,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "progress", headerName: "Progresso", width: 160,
      renderCell: (p) => {
        const pct = p.row.targetAmount ? Math.min(100, Math.round(((p.row.achievedAmount || 0) / p.row.targetAmount) * 100)) : 0;
        return (
          <Box sx={{ width: "100%", display: "flex", alignItems: "center", gap: 1 }}>
            <LinearProgress variant="determinate" value={pct} sx={{ flex: 1, height: 8, borderRadius: 4 }} color={pct >= 100 ? "success" : "primary"} />
            <Typography variant="caption" fontWeight="bold">{pct}%</Typography>
          </Box>
        );
      },
    },
    { field: "startDate", headerName: "Início", width: 110 },
    { field: "endDate", headerName: "Fim", width: 110 },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => <Chip label={statusLabel[p.value] || p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 120, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {p.row.status === "ACTIVE" && (
            <>
              <Tooltip title="Marcar Completa"><IconButton size="small" color="success" onClick={() => handleComplete(p.row.id)}><CheckCircle fontSize="small" /></IconButton></Tooltip>
              <Tooltip title="Cancelar"><IconButton size="small" color="error" onClick={() => handleCancel(p.row.id)}><CancelIcon fontSize="small" /></IconButton></Tooltip>
            </>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Metas de Vendas</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => {
          setForm({ sellerId: "", targetType: "INDIVIDUAL", targetAmount: "", startDate: "", endDate: "", description: "" });
          setOpen(true);
        }}>Nova Meta</Button>
      </Box>
      <Box sx={{ height: 600, bgcolor: "#fff", borderRadius: 2 }}>
        <DataGrid rows={rows} columns={columns} loading={loading}
          pageSizeOptions={[10, 25, 50]} initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick slots={{ toolbar: GridToolbar }}
          slotProps={{ toolbar: { showQuickFilter: true, quickFilterProps: { debounceMs: 300 } } }}
          sx={{ border: "1px solid #e0e0e0" }}
        />
      </Box>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Nova Meta de Vendas</DialogTitle>
        <DialogContent dividers>
          <TextField label="Descrição" fullWidth margin="normal" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required />
          <TextField select label="Tipo" fullWidth margin="normal" value={form.targetType} onChange={(e) => setForm({ ...form, targetType: e.target.value })}>
            {targetTypes.map((t) => <MenuItem key={t} value={t}>{t === "INDIVIDUAL" ? "Individual" : t === "TEAM" ? "Equipe" : "Empresa"}</MenuItem>)}
          </TextField>
          <TextField label="Vendedor ID" fullWidth margin="normal" type="number" value={form.sellerId} onChange={(e) => setForm({ ...form, sellerId: e.target.value })} helperText="Obrigatório para metas individuais" />
          <TextField label="Valor Meta (R$)" fullWidth margin="normal" type="number" value={form.targetAmount} onChange={(e) => setForm({ ...form, targetAmount: e.target.value })} required />
          <Box display="flex" gap={2}>
            <TextField label="Data Início" fullWidth margin="normal" type="date" InputLabelProps={{ shrink: true }} value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} required />
            <TextField label="Data Fim" fullWidth margin="normal" type="date" InputLabelProps={{ shrink: true }} value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} required />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>{saving ? "Criando..." : "Criar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
