import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

export default function Warehouses() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: "", location: "" });
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/warehouses/company/${companyId}`)
      .then((r) => setRows(r.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ name: row.name || "", location: row.location || "" });
    } else {
      setEditing(null);
      setForm({ name: "", location: "" });
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = { companyId, name: form.name, location: form.location, active: true };
      if (editing) await api.put(`/warehouses/${editing.id}`, payload);
      else await api.post("/warehouses", payload);
      setOpen(false);
      fetchData();
    } catch (err) { alert(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm("Desativar este armazém?")) return;
    try { await api.delete(`/warehouses/${id}`); fetchData(); } catch (err) { alert(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "name", headerName: "Nome", flex: 1, minWidth: 200 },
    { field: "location", headerName: "Localização", flex: 1, minWidth: 200 },
    {
      field: "active", headerName: "Status", width: 120,
      renderCell: (p) => <Chip label={p.value ? "Ativo" : "Inativo"} color={p.value ? "success" : "default"} size="small" />,
    },
    {
      field: "actions", headerName: "Ações", width: 120, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>
          {p.row.active && <Tooltip title="Desativar"><IconButton size="small" color="error" onClick={() => handleDeactivate(p.row.id)}><Delete fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Armazéns</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Armazém</Button>
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
        <DialogTitle>{editing ? "Editar Armazém" : "Novo Armazém"}</DialogTitle>
        <DialogContent dividers>
          <TextField label="Nome" fullWidth margin="normal" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <TextField label="Localização" fullWidth margin="normal" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
