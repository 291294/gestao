import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, CheckCircle, Cancel as CancelIcon, Undo } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

const METHODS = ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "PIX", "BOLETO", "CHECK"];
const methodLabel = { CASH: "Dinheiro", CREDIT_CARD: "Crédito", DEBIT_CARD: "Débito", BANK_TRANSFER: "Transferência", PIX: "PIX", BOLETO: "Boleto", CHECK: "Cheque" };
const statusColor = { PENDING: "warning", CONFIRMED: "success", REFUNDED: "info", CANCELLED: "error" };

export default function Payments() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ invoiceId: "", amount: "", paymentMethod: "PIX", notes: "" });
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/payments/company/${companyId}`)
      .then((r) => setRows(r.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    setSaving(true);
    try {
      await api.post("/payments", { companyId, invoiceId: Number(form.invoiceId), amount: Number(form.amount), paymentMethod: form.paymentMethod, notes: form.notes });
      setOpen(false);
      showSuccess("Pagamento criado com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao criar pagamento"); }
    finally { setSaving(false); }
  };

  const handleAction = async (id, action) => {
    try { await api.post(`/payments/${id}/${action}`); showSuccess("Operação realizada com sucesso!"); fetchData(); }
    catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "paymentNumber", headerName: "Nº Pagamento", width: 160 },
    {
      field: "amount", headerName: "Valor", width: 130,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "paymentMethod", headerName: "Método", width: 130,
      valueFormatter: (v) => methodLabel[v] || v,
    },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => <Chip label={p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    { field: "invoiceId", headerName: "Fatura ID", width: 100 },
    {
      field: "actions", headerName: "Ações", width: 150, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {p.row.status === "PENDING" && (
            <>
              <Tooltip title="Confirmar"><IconButton size="small" color="success" onClick={() => handleAction(p.row.id, "confirm")}><CheckCircle fontSize="small" /></IconButton></Tooltip>
              <Tooltip title="Cancelar"><IconButton size="small" color="error" onClick={() => handleAction(p.row.id, "cancel")}><CancelIcon fontSize="small" /></IconButton></Tooltip>
            </>
          )}
          {p.row.status === "CONFIRMED" && (
            <Tooltip title="Reembolsar"><IconButton size="small" color="info" onClick={() => handleAction(p.row.id, "refund")}><Undo fontSize="small" /></IconButton></Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Pagamentos</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { setForm({ invoiceId: "", amount: "", paymentMethod: "PIX", notes: "" }); setOpen(true); }}>Novo Pagamento</Button>
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
        <DialogTitle>Novo Pagamento</DialogTitle>
        <DialogContent dividers>
          <TextField label="ID da Fatura" fullWidth margin="normal" type="number" value={form.invoiceId} onChange={(e) => setForm({ ...form, invoiceId: e.target.value })} required />
          <TextField label="Valor (R$)" fullWidth margin="normal" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
          <TextField select label="Método" fullWidth margin="normal" value={form.paymentMethod} onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}>
            {METHODS.map((m) => <MenuItem key={m} value={m}>{methodLabel[m]}</MenuItem>)}
          </TextField>
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
