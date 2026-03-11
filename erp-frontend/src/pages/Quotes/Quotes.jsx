import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete } from "@mui/icons-material";
import api from "../../api/apiClient";

const statusColor = { DRAFT: "default", SENT: "info", APPROVED: "success", REJECTED: "error", EXPIRED: "warning" };

export default function Quotes() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clients, setClients] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ clientName: "", status: "DRAFT", totalAmount: "" });
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get("/quotes").then((r) => setRows(r.data || [])).catch(() => setRows([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchData();
    api.get("/clients").then((r) => setClients(r.data || [])).catch(() => {});
  }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ clientName: row.clientName || "", status: row.status || "DRAFT", totalAmount: row.totalAmount || "" });
    } else {
      setEditing(null);
      setForm({ clientName: "", status: "DRAFT", totalAmount: "" });
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (editing) await api.put(`/quotes/${editing.id}`, form);
      else await api.post("/quotes", form);
      setOpen(false);
      fetchData();
    } catch (err) { alert(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este orçamento?")) return;
    try { await api.delete(`/quotes/${id}`); fetchData(); } catch (err) { alert(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "clientName", headerName: "Cliente", flex: 1, minWidth: 200 },
    {
      field: "totalAmount", headerName: "Valor Total", width: 150,
      valueFormatter: (v) => `R$ ${(v || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`,
    },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => <Chip label={p.value} color={statusColor[p.value] || "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 120, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>
          <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => handleDelete(p.row.id)}><Delete fontSize="small" /></IconButton></Tooltip>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Orçamentos</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Orçamento</Button>
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
        <DialogTitle>{editing ? "Editar Orçamento" : "Novo Orçamento"}</DialogTitle>
        <DialogContent dividers>
          <TextField select label="Cliente" fullWidth margin="normal" value={form.clientName} onChange={(e) => setForm({ ...form, clientName: e.target.value })}>
            {clients.map((c) => <MenuItem key={c.id} value={c.name}>{c.name}</MenuItem>)}
          </TextField>
          <TextField label="Valor Total" fullWidth margin="normal" type="number" value={form.totalAmount} onChange={(e) => setForm({ ...form, totalAmount: e.target.value })} />
          <TextField select label="Status" fullWidth margin="normal" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            {Object.keys(statusColor).map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
