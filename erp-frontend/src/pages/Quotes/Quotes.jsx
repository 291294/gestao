import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete, ThumbUp, ThumbDown, ShoppingCart } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const statusColor = { DRAFT: "default", SENT: "info", APPROVED: "success", REJECTED: "error", EXPIRED: "warning", CONVERTED: "secondary" };
const statusLabel = { DRAFT: "Rascunho", SENT: "Enviado", APPROVED: "Aprovado", REJECTED: "Rejeitado", EXPIRED: "Expirado", CONVERTED: "Convertido" };
const emptyItem = { productId: "", quantity: "", unitPrice: "", description: "" };

export default function Quotes() {
  const { companyId, checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clients, setClients] = useState([]);
  const [products, setProducts] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ clientId: "", status: "DRAFT" });
  const [items, setItems] = useState([{ ...emptyItem }]);
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const canCreate = checkPermission("quote", "create");
  const canEdit = checkPermission("quote", "update");
  const canDelete = checkPermission("quote", "delete");

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/quotes/company/${companyId}`).then((r) => {
      const data = r.data?.content || r.data || [];
      setRows(Array.isArray(data) ? data : []);
    }).catch(() => setRows([])).finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => {
    fetchData();
    api.get("/clients").then((r) => setClients(r.data || [])).catch(() => {});
    api.get("/products").then((r) => setProducts(r.data || [])).catch(() => {});
  }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ clientId: row.clientId || "", status: row.status || "DRAFT" });
      setItems(
        row.items?.length
          ? row.items.map((i) => ({ productId: i.productId || "", quantity: i.quantity || "", unitPrice: i.unitPrice || "", description: i.description || "" }))
          : [{ ...emptyItem }]
      );
    } else {
      setEditing(null);
      setForm({ clientId: "", status: "DRAFT" });
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
        items: items.filter((i) => i.productId).map((i) => ({
          productId: Number(i.productId), quantity: Number(i.quantity), unitPrice: Number(i.unitPrice), description: i.description || "",
        })),
      };
      if (editing) {
        await api.patch(`/quotes/${editing.id}/status?status=${form.status}`);
      } else {
        await api.post("/quotes", payload);
      }
      setOpen(false);
      showSuccess(editing ? "Orçamento atualizado com sucesso!" : "Orçamento criado com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este orçamento?")) return;
    try { await api.delete(`/quotes/${id}`); showSuccess("Orçamento excluído com sucesso!"); fetchData(); } catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const handleApprove = async (id) => {
    try { await api.post(`/quotes/${id}/approve`); showSuccess("Orçamento aprovado com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao aprovar"); }
  };

  const handleReject = async (id) => {
    try { await api.post(`/quotes/${id}/reject`); showSuccess("Orçamento rejeitado!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao rejeitar"); }
  };

  const handleConvert = async (id) => {
    if (!window.confirm("Converter orçamento em pedido?")) return;
    try { await api.post(`/quotes/${id}/convert?commissionPercentage=5.00`); showSuccess("Orçamento convertido em pedido com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro ao converter"); }
  };

  const updateItem = (idx, field, val) => { const n = [...items]; n[idx] = { ...n[idx], [field]: val }; setItems(n); };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "clientName", headerName: "Cliente", flex: 1, minWidth: 180 },
    {
      field: "totalAmount", headerName: "Valor Total", width: 150,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => <Chip label={statusLabel[p.value] || p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 200, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {canEdit && p.row.status === "DRAFT" && (
            <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>
          )}
          {p.row.status === "SENT" && (
            <>
              <Tooltip title="Aprovar"><IconButton size="small" color="success" onClick={() => handleApprove(p.row.id)}><ThumbUp fontSize="small" /></IconButton></Tooltip>
              <Tooltip title="Rejeitar"><IconButton size="small" color="error" onClick={() => handleReject(p.row.id)}><ThumbDown fontSize="small" /></IconButton></Tooltip>
            </>
          )}
          {p.row.status === "APPROVED" && (
            <Tooltip title="Converter em Pedido"><IconButton size="small" color="primary" onClick={() => handleConvert(p.row.id)}><ShoppingCart fontSize="small" /></IconButton></Tooltip>
          )}
          {canDelete && (p.row.status === "DRAFT" || p.row.status === "REJECTED") && (
            <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => handleDelete(p.row.id)}><Delete fontSize="small" /></IconButton></Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Orçamentos</Typography>
        {canCreate && <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Orçamento</Button>}
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
        <DialogTitle>{editing ? "Editar Orçamento" : "Novo Orçamento"}</DialogTitle>
        <DialogContent dividers>
          <Box display="flex" gap={2} mb={2} mt={1}>
            <TextField select label="Cliente" fullWidth value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} required>
              {clients.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
            </TextField>
            {editing && (
              <TextField select label="Status" fullWidth value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                {Object.keys(statusColor).map((s) => <MenuItem key={s} value={s}>{statusLabel[s] || s}</MenuItem>)}
              </TextField>
            )}
          </Box>
          {!editing && (
            <>
              <Typography variant="subtitle1" fontWeight="bold" mb={1}>Itens do Orçamento</Typography>
              {items.map((item, idx) => (
                <Box key={idx} display="flex" gap={1} mb={1} alignItems="center">
                  <TextField select label="Produto" value={item.productId} onChange={(e) => updateItem(idx, "productId", e.target.value)} sx={{ minWidth: 200 }} size="small">
                    {products.map((p) => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
                  </TextField>
                  <TextField label="Qtd" type="number" value={item.quantity} onChange={(e) => updateItem(idx, "quantity", e.target.value)} size="small" sx={{ width: 100 }} />
                  <TextField label="Preço Unit." type="number" value={item.unitPrice} onChange={(e) => updateItem(idx, "unitPrice", e.target.value)} size="small" sx={{ width: 130 }} />
                  <TextField label="Descrição" value={item.description} onChange={(e) => updateItem(idx, "description", e.target.value)} size="small" sx={{ flex: 1 }} />
                  <IconButton size="small" color="error" onClick={() => setItems(items.filter((_, i) => i !== idx))} disabled={items.length === 1}><Delete fontSize="small" /></IconButton>
                </Box>
              ))}
              <Button size="small" onClick={() => setItems([...items, { ...emptyItem }])} sx={{ mt: 1 }}>+ Adicionar Item</Button>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
