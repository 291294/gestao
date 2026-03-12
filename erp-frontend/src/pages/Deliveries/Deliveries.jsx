import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, LocalShipping, CheckCircle, Cancel as CancelIcon } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const statusColor = {
  PENDING: "default", SCHEDULED: "info", IN_TRANSIT: "warning",
  DELIVERED: "success", CANCELLED: "error",
};
const statusLabel = {
  PENDING: "Pendente", SCHEDULED: "Agendada", IN_TRANSIT: "Em Trânsito",
  DELIVERED: "Entregue", CANCELLED: "Cancelada",
};

export default function Deliveries() {
  const { companyId, checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ orderId: "", deliveryAddress: "", scheduledDate: "", notes: "" });
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const canCreate = checkPermission("delivery", "create");

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/deliveries/company/${companyId}/status/PENDING`)
      .then((r) => setRows(r.data || []))
      .catch(() => {
        api.get("/deliveries/order/0").then(() => setRows([])).catch(() => setRows([]));
      })
      .finally(() => setLoading(false));
    // Carrega todas as entregas (pendentes, em trânsito, entregues)
    Promise.all([
      api.get(`/deliveries/company/${companyId}/status/PENDING`).catch(() => ({ data: [] })),
      api.get(`/deliveries/company/${companyId}/status/SCHEDULED`).catch(() => ({ data: [] })),
      api.get(`/deliveries/company/${companyId}/in-transit`).catch(() => ({ data: [] })),
      api.get(`/deliveries/company/${companyId}/status/DELIVERED`).catch(() => ({ data: [] })),
    ]).then(([p, s, t, d]) => {
      const all = [...(p.data || []), ...(s.data || []), ...(t.data || []), ...(d.data || [])];
      const unique = Array.from(new Map(all.map(i => [i.id, i])).values());
      setRows(unique);
    }).catch(() => {}).finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => {
    fetchData();
    api.get("/orders").then((r) => setOrders(r.data || [])).catch(() => {});
  }, [fetchData]);

  const handleShip = async (id) => {
    try { await api.post(`/deliveries/${id}/ship`); showSuccess("Entrega despachada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao despachar"); }
  };

  const handleDeliver = async (id) => {
    try { await api.post(`/deliveries/${id}/deliver`); showSuccess("Entrega confirmada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao confirmar entrega"); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancelar esta entrega?")) return;
    try { await api.post(`/deliveries/${id}/cancel`); showSuccess("Entrega cancelada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao cancelar"); }
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      const payload = {
        orderId: Number(form.orderId),
        companyId,
        deliveryAddress: form.deliveryAddress,
        scheduledDate: form.scheduledDate || null,
        notes: form.notes,
      };
      await api.post("/deliveries", payload);
      setOpen(false);
      showSuccess("Entrega criada com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao criar entrega"); }
    finally { setSaving(false); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "orderId", headerName: "Pedido", width: 100 },
    { field: "deliveryAddress", headerName: "Endereço", flex: 1, minWidth: 200 },
    { field: "scheduledDate", headerName: "Data Agendada", width: 150 },
    { field: "deliveredAt", headerName: "Entregue em", width: 150 },
    {
      field: "status", headerName: "Status", width: 140,
      renderCell: (p) => <Chip label={statusLabel[p.value] || p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 160, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {(p.row.status === "PENDING" || p.row.status === "SCHEDULED") && (
            <Tooltip title="Despachar"><IconButton size="small" color="primary" onClick={() => handleShip(p.row.id)}><LocalShipping fontSize="small" /></IconButton></Tooltip>
          )}
          {p.row.status === "IN_TRANSIT" && (
            <Tooltip title="Confirmar Entrega"><IconButton size="small" color="success" onClick={() => handleDeliver(p.row.id)}><CheckCircle fontSize="small" /></IconButton></Tooltip>
          )}
          {p.row.status !== "DELIVERED" && p.row.status !== "CANCELLED" && (
            <Tooltip title="Cancelar"><IconButton size="small" color="error" onClick={() => handleCancel(p.row.id)}><CancelIcon fontSize="small" /></IconButton></Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Entregas</Typography>
        {canCreate && (
          <Button variant="contained" startIcon={<Add />} onClick={() => {
            setForm({ orderId: "", deliveryAddress: "", scheduledDate: "", notes: "" });
            setOpen(true);
          }}>Nova Entrega</Button>
        )}
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
        <DialogTitle>Nova Entrega</DialogTitle>
        <DialogContent dividers>
          <TextField select label="Pedido" fullWidth margin="normal" value={form.orderId} onChange={(e) => setForm({ ...form, orderId: e.target.value })} required>
            {orders.map((o) => <MenuItem key={o.id} value={o.id}>#{o.id} — {o.client?.name || o.clientName || "Cliente"}</MenuItem>)}
          </TextField>
          <TextField label="Endereço de Entrega" fullWidth margin="normal" multiline rows={2} value={form.deliveryAddress} onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} required />
          <TextField label="Data Agendada" fullWidth margin="normal" type="date" InputLabelProps={{ shrink: true }} value={form.scheduledDate} onChange={(e) => setForm({ ...form, scheduledDate: e.target.value })} />
          <TextField label="Observações" fullWidth margin="normal" multiline rows={2} value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>{saving ? "Criando..." : "Criar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
