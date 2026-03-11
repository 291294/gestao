import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, PlayArrow, CheckCircle } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

const statusColor = { CREATED: "default", IN_PROGRESS: "info", FINISHED: "success", CANCELLED: "error" };

export default function Manufacturing() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ productId: "", quantity: "" });
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get("/manufacturing/production-orders")
      .then((r) => setRows(Array.isArray(r.data) ? r.data : []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    setSaving(true);
    try {
      await api.post(`/manufacturing/production-order?companyId=${companyId}&productId=${form.productId}&quantity=${form.quantity}`);
      setOpen(false);
      fetchData();
    } catch (err) { alert(err.response?.data?.message || "Erro ao criar OP"); }
    finally { setSaving(false); }
  };

  const handleStart = async (id) => {
    try { await api.post(`/manufacturing/production-order/${id}/start`); fetchData(); }
    catch (err) { alert(err.response?.data?.message || "Erro ao iniciar"); }
  };

  const handleFinish = async (id) => {
    try { await api.post(`/manufacturing/production-order/${id}/finish`); fetchData(); }
    catch (err) { alert(err.response?.data?.message || "Erro ao finalizar"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "productId", headerName: "Produto ID", width: 120 },
    { field: "quantity", headerName: "Quantidade", width: 120, type: "number" },
    {
      field: "status", headerName: "Status", width: 140,
      renderCell: (p) => <Chip label={p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 150, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {p.row.status === "CREATED" && (
            <Tooltip title="Iniciar Produção"><IconButton size="small" color="primary" onClick={() => handleStart(p.row.id)}><PlayArrow fontSize="small" /></IconButton></Tooltip>
          )}
          {p.row.status === "IN_PROGRESS" && (
            <Tooltip title="Finalizar"><IconButton size="small" color="success" onClick={() => handleFinish(p.row.id)}><CheckCircle fontSize="small" /></IconButton></Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Produção</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { setForm({ productId: "", quantity: "" }); setOpen(true); }}>Nova Ordem</Button>
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
        <DialogTitle>Nova Ordem de Produção</DialogTitle>
        <DialogContent dividers>
          <TextField label="Produto ID" fullWidth margin="normal" type="number" value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} required />
          <TextField label="Quantidade" fullWidth margin="normal" type="number" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} required />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>{saving ? "Criando..." : "Criar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
