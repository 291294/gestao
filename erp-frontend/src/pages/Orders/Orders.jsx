import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete, Cancel as CancelIcon } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const STATUS_OPTIONS = ["PENDING", "CONFIRMED", "DELIVERED", "CANCELLED"];
const statusColor = { PENDING: "warning", CONFIRMED: "info", DELIVERED: "success", CANCELLED: "error" };
const emptyItem = { productId: "", quantity: "", unitPrice: "", notes: "" };

export default function Orders() {
  const { companyId, checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clients, setClients] = useState([]);
  const [products, setProducts] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ clientId: "", status: "PENDING" });
  const [items, setItems] = useState([{ ...emptyItem }]);
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const canCreate = checkPermission("order", "create");
  const canEdit = checkPermission("order", "update");
  const canDelete = checkPermission("order", "delete");

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get("/orders").then((r) => setRows(r.data || [])).catch(() => setRows([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchData();
    api.get("/clients").then((r) => setClients(r.data || [])).catch(() => {});
    api.get("/products").then((r) => setProducts(r.data || [])).catch(() => {});
  }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ clientId: row.client?.id || row.clientId || "", status: row.status || "PENDING" });
      setItems(
        row.items?.length
          ? row.items.map((i) => ({ productId: i.productId || "", quantity: i.quantity || "", unitPrice: i.unitPrice || "", notes: i.notes || "" }))
          : [{ ...emptyItem }]
      );
    } else {
      setEditing(null);
      setForm({ clientId: "", status: "PENDING" });
      setItems([{ ...emptyItem }]);
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        companyId,
        clientId: Number(form.clientId),
        status: form.status,
        totalValue: items.reduce((s, i) => s + (Number(i.quantity) || 0) * (Number(i.unitPrice) || 0), 0),
        items: items.filter((i) => i.productId).map((i) => ({
          productId: Number(i.productId), quantity: Number(i.quantity), unitPrice: Number(i.unitPrice), notes: i.notes || "",
        })),
      };
      if (editing) await api.put(`/orders/${editing.id}`, payload);
      else await api.post("/orders", payload);
      setOpen(false);
      showSuccess(editing ? "Pedido atualizado com sucesso!" : "Pedido criado com sucesso!");
      fetchData();
    } catch (err) {
      showError(err.response?.data?.message || "Erro ao salvar pedido");
    } finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este pedido?")) return;
    try { await api.delete(`/orders/${id}`); showSuccess("Pedido excluído com sucesso!"); fetchData(); } catch (err) { showError(err.response?.data?.message || "Erro ao excluir"); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancelar este pedido?")) return;
    try { await api.post(`/orders/${id}/cancel`); showSuccess("Pedido cancelado com sucesso!"); fetchData(); } catch (err) { showError(err.response?.data?.message || "Erro ao cancelar"); }
  };

  const updateItem = (idx, field, val) => { const n = [...items]; n[idx] = { ...n[idx], [field]: val }; setItems(n); };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "clientName", headerName: "Cliente", flex: 1, minWidth: 180, valueGetter: (v, row) => row.client?.name || "—" },
    {
      field: "totalValue", headerName: "Valor Total", width: 150,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "status", headerName: "Status", width: 140,
      renderCell: (p) => <Chip label={p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 150, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {canEdit && <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>}
          {canEdit && p.row.status !== "CANCELLED" && (
            <Tooltip title="Cancelar"><IconButton size="small" color="warning" onClick={() => handleCancel(p.row.id)}><CancelIcon fontSize="small" /></IconButton></Tooltip>
          )}
          {canDelete && <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => handleDelete(p.row.id)}><Delete fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Pedidos</Typography>
        {canCreate && <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Pedido</Button>}
      </Box>

      <Box sx={{ height: 600, bgcolor: "#fff", borderRadius: 2 }}>
        <DataGrid rows={rows} columns={columns} loading={loading}
          pageSizeOptions={[10, 25, 50]} initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick slots={{ toolbar: GridToolbar }}
          slotProps={{ toolbar: { showQuickFilter: true, quickFilterProps: { debounceMs: 300 } } }}
          sx={{ border: "1px solid #e0e0e0" }}
        />
      </Box>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{editing ? "Editar Pedido" : "Novo Pedido"}</DialogTitle>
        <DialogContent dividers>
          <Box display="flex" gap={2} mb={2} mt={1}>
            <TextField select label="Cliente" fullWidth value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} required>
              {clients.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
            </TextField>
            <TextField select label="Status" fullWidth value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
          </Box>
          <Typography variant="subtitle1" fontWeight="bold" mb={1}>Itens do Pedido</Typography>
          {items.map((item, idx) => (
            <Box key={idx} display="flex" gap={1} mb={1} alignItems="center">
              <TextField select label="Produto" value={item.productId} onChange={(e) => updateItem(idx, "productId", e.target.value)} sx={{ minWidth: 200 }} size="small">
                {products.map((p) => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
              </TextField>
              <TextField label="Qtd" type="number" value={item.quantity} onChange={(e) => updateItem(idx, "quantity", e.target.value)} size="small" sx={{ width: 100 }} />
              <TextField label="Preço Unit." type="number" value={item.unitPrice} onChange={(e) => updateItem(idx, "unitPrice", e.target.value)} size="small" sx={{ width: 130 }} />
              <TextField label="Obs" value={item.notes} onChange={(e) => updateItem(idx, "notes", e.target.value)} size="small" sx={{ flex: 1 }} />
              <IconButton size="small" color="error" onClick={() => setItems(items.filter((_, i) => i !== idx))} disabled={items.length === 1}><Delete fontSize="small" /></IconButton>
            </Box>
          ))}
          <Button size="small" onClick={() => setItems([...items, { ...emptyItem }])} sx={{ mt: 1 }}>+ Adicionar Item</Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
