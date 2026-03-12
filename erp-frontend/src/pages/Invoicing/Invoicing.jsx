import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Send, Cancel as CancelIcon, Receipt } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const statusColor = { DRAFT: "default", ISSUED: "info", SENT: "primary", PAID: "success", PARTIALLY_PAID: "warning", OVERDUE: "error", CANCELLED: "default" };

export default function Invoicing() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clients, setClients] = useState([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ clientId: "", dueDate: "", notes: "", items: [] });
  const [items, setItems] = useState([{ productId: "", description: "", quantity: "", unitPrice: "" }]);
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/invoices/company/${companyId}`)
      .then((r) => {
        const data = r.data?.content || r.data || [];
        setRows(Array.isArray(data) ? data : []);
      })
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => {
    fetchData();
    api.get("/clients").then((r) => setClients(r.data || [])).catch(() => {});
  }, [fetchData]);

  const handleCreate = async () => {
    setSaving(true);
    try {
      const payload = {
        companyId, clientId: Number(form.clientId), dueDate: form.dueDate || null, notes: form.notes,
        items: items.filter((i) => i.productId).map((i) => ({
          productId: Number(i.productId), description: i.description, quantity: Number(i.quantity), unitPrice: Number(i.unitPrice),
        })),
      };
      await api.post("/invoices", payload);
      setOpen(false);
      showSuccess("Fatura criada com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao criar fatura"); }
    finally { setSaving(false); }
  };

  const handleAction = async (id, action) => {
    try { await api.post(`/invoices/${id}/${action}`); showSuccess("Operação realizada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "invoiceNumber", headerName: "Nº Fatura", width: 150 },
    { field: "clientName", headerName: "Cliente", flex: 1, minWidth: 180 },
    {
      field: "totalAmount", headerName: "Total", width: 130,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "amountPaid", headerName: "Pago", width: 130,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    { field: "dueDate", headerName: "Vencimento", width: 120 },
    {
      field: "status", headerName: "Status", width: 150,
      renderCell: (p) => <Chip label={p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 150, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {p.row.status === "DRAFT" && <Tooltip title="Emitir"><IconButton size="small" color="primary" onClick={() => handleAction(p.row.id, "issue")}><Receipt fontSize="small" /></IconButton></Tooltip>}
          {p.row.status === "ISSUED" && <Tooltip title="Enviar"><IconButton size="small" color="info" onClick={() => handleAction(p.row.id, "send")}><Send fontSize="small" /></IconButton></Tooltip>}
          {!["CANCELLED", "PAID"].includes(p.row.status) && <Tooltip title="Cancelar"><IconButton size="small" color="error" onClick={() => handleAction(p.row.id, "cancel")}><CancelIcon fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Faturamento</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { setForm({ clientId: "", dueDate: "", notes: "" }); setItems([{ productId: "", description: "", quantity: "", unitPrice: "" }]); setOpen(true); }}>Nova Fatura</Button>
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
        <DialogTitle>Nova Fatura</DialogTitle>
        <DialogContent dividers>
          <Box display="flex" gap={2} mb={2} mt={1}>
            <TextField select label="Cliente" fullWidth value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} required>
              {clients.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
            </TextField>
            <TextField label="Vencimento" type="date" fullWidth value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} slotProps={{ inputLabel: { shrink: true } }} />
          </Box>
          <TextField label="Observações" fullWidth margin="normal" multiline rows={2} value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
          <Typography variant="subtitle1" fontWeight="bold" mt={2} mb={1}>Itens</Typography>
          {items.map((item, idx) => (
            <Box key={idx} display="flex" gap={1} mb={1}>
              <TextField label="Produto ID" type="number" value={item.productId} onChange={(e) => { const n = [...items]; n[idx].productId = e.target.value; setItems(n); }} size="small" sx={{ width: 120 }} />
              <TextField label="Descrição" value={item.description} onChange={(e) => { const n = [...items]; n[idx].description = e.target.value; setItems(n); }} size="small" sx={{ flex: 1 }} />
              <TextField label="Qtd" type="number" value={item.quantity} onChange={(e) => { const n = [...items]; n[idx].quantity = e.target.value; setItems(n); }} size="small" sx={{ width: 80 }} />
              <TextField label="Preço" type="number" value={item.unitPrice} onChange={(e) => { const n = [...items]; n[idx].unitPrice = e.target.value; setItems(n); }} size="small" sx={{ width: 120 }} />
              <IconButton size="small" color="error" onClick={() => setItems(items.filter((_, i) => i !== idx))} disabled={items.length === 1}><CancelIcon fontSize="small" /></IconButton>
            </Box>
          ))}
          <Button size="small" onClick={() => setItems([...items, { productId: "", description: "", quantity: "", unitPrice: "" }])}>+ Item</Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>{saving ? "Criando..." : "Criar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
